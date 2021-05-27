package zandbox.internal.hooks2.other;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantLock;

import app.packed.container.InternalExtensionException;
import app.packed.hooks.OldFieldHook;
import packed.internal.hooks.usesite.UseSiteFieldHookModel;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

public record FieldBootstrapModel(MethodHandle constructor) {

    /** A map that contains Bootstrap, Builder or Throwable */
    private static final WeakHashMap<Class<? extends OldFieldHook.Bootstrap>, Object> DATA = new WeakHashMap<>();

    /** A lock used for making sure that we only load one extension (and its dependencies) at a time. */
    private static final ReentrantLock GLOBAL_LOCK = new ReentrantLock();

    /** A MethodHandle that can invoke {@link OldFieldHook.Bootstrap#bootstrap}. */
    private static final MethodHandle MH_FIELD_HOOK_BOOTSTRAP = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), OldFieldHook.Bootstrap.class, "bootstrap",
            void.class);

    /** A VarHandle that can access {@link OldFieldHook.Bootstrap#processor}. */
    private static final VarHandle VH_FIELD_HOOK_BUILDER = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), OldFieldHook.Bootstrap.class, "builder",
            UseSiteFieldHookModel.Builder.class);

    /**
     * @param callerClass
     *            the calling class (must a proper subclass of Extension)
     * @return
     */
    public static Builder bootstrap(Class<?> callerClass) {
        if (!OldFieldHook.Bootstrap.class.isAssignableFrom(callerClass) || callerClass == OldFieldHook.Bootstrap.class) {
            throw new InternalExtensionException("This method can only be called directly from a subclass of FieldHook.Bootstrap, caller was " + callerClass);
        }
        @SuppressWarnings("unchecked")
        Class<? extends OldFieldHook.Bootstrap> extensionClass = (Class<? extends OldFieldHook.Bootstrap>) callerClass;
        GLOBAL_LOCK.lock();
        try {
            Object m = DATA.get(callerClass);
            if (m == null) {
                Builder b = new Builder();
                DATA.put(extensionClass, b);
                return b;
            } else if (m instanceof Builder b) {
                return b;
            } else {
                throw new IllegalStateException("This method must be called from within the class initializer of a field bootstrap class, class = " + callerClass);
            }
        } finally {
            GLOBAL_LOCK.unlock();
        }
    }

    public static void bootstrap(OldFieldHook.Bootstrap instance, UseSiteFieldHookModel.Builder builder) {
        VH_FIELD_HOOK_BUILDER.set(instance, builder);
        try {
            MH_FIELD_HOOK_BOOTSTRAP.invoke(instance); // Invokes FieldHook.Bootstrap#bootstrap()
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    public static final class Builder {

    }
}
