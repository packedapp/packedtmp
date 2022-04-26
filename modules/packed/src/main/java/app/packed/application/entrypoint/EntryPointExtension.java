package app.packed.application.entrypoint;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import app.packed.bean.ContainerBeanConfiguration;
import app.packed.bean.hooks.BeanMethod;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionConfiguration;
import packed.internal.application.EntryPointSetup;
import packed.internal.application.EntryPointSetup.MainThreadOfControl;
import packed.internal.bean.hooks.PackedBeanMethod;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionSetup;

// Cannot use both @app.packed.application.Main and the CLI extension at the same time.
/**
 * An extension that controls any entry points of the an application.
 */
public class EntryPointExtension extends Extension<EntryPointExtension> {

    private final ContainerSetup container;

    boolean hasMain;

    Shared shared;

    /**
     * Create a new service extension.
     * 
     * @param configuration
     *            an extension configuration object.
     */
    /* package-private */ EntryPointExtension(ExtensionConfiguration configuration) {
        this.container = ((ExtensionSetup) configuration).container;
        //    
    }

    @Override
    protected void hookOnBeanMethod(BeanMethod method) {
        registerEntryPoint(null, true);

        container.application.entryPoints = new EntryPointSetup();

        MainThreadOfControl mc = container.application.entryPoints.mainThread();
        mc.isStatic = Modifier.isStatic(method.getModifiers());
        mc.cs = ((PackedBeanMethod) method).bean;
        mc.methodHandle = method.newRawOperation().handle();

//        oldOperation().useMirror(() -> new EntryPointMirror(0));
    }

    public void main(Runnable runnable) {
        // Det her er en function
    }

    public <T extends Runnable> ContainerBeanConfiguration<?> mainBeanInstance(T runnable) {
        // Hehe, tag den, hvad goer vi her, den ene er en bean
        // Den anden er en function.
        // Tror ikke vi skal have begge dele
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public EntryPointExtensionMirror mirror() {
        return mirrorInitialize(new EntryPointExtensionMirror());
    }

    @Override
    protected void onApplicationClose() {
        if (isRootOfApplication()) {
            // Her installere vi MethodHandles der bliver shared, taenker det er bedre end at faa injected
            // extensions'ene

            // install
            // provide (visible i mxxz;ias:"?aZ.a:n aZz¸ m nl jj m ,m ;n .n ≥ en container)
            // provideShared (container + subcontainers)
            shareInstance(new MethodHandle[0]);
        }
        super.onApplicationClose();
    }

    protected void onNew() {
        shared = configuration().isRootOfApplication() ? new Shared() : treeOfApplication().root().shared;
    }

    int registerEntryPoint(Class<? extends Extension<?>> extensionType, boolean isMain) {

        // Jeg gaar udfra metoden er blevet populeret med hvad der er behov for.
        // Saa det er kun selve invokationen der sker her

        // method.reserveMethodHandle(EC);
        // Grim kode pfa ExtensoinSupportContest i constructoren
        if (extensionType == null) {
            shared().takeOver(EntryPointExtension.class);
        } else {
            shared().takeOver(extensionType);
        }
        if (isMain) {
            hasMain = true;
        }
        return 0;
    }

    public void setShutdownStrategy(Supplier<Throwable> maker) {
        // Ideen er lidt at sige hvad sker der paa runtime.
        // Hvis vi shutter down... Default er at der bliver
        // Sat en cancellation exception som reaason
        // CancellationException
        // Men ved ikke om vi skal have en special exception istedet for???
        // En checked istedet ligesom TimeoutException
    }

    Shared shared() {
        if (isRootOfApplication()) {
            Shared s = shared;
            if (s == null) {
                s = shared = new Shared();
            }
            return s;
        } else {
            return treeOfApplication().root().shared();
        }
    }

    static class EntryPointConf {

    }

    /** An instance of this class is shared between all entry point extensions for a single application. */
    static class Shared {
        
        /** All entry points. */
        final List<EntryPointConf> entrypoints = new ArrayList<>();
        
        MethodHandle[] entryPoints;
        Class<? extends Extension<?>> takeOver;

        void takeOver(Class<? extends Extension<?>> takeOver) {
            if (this.takeOver != null) {
                if (takeOver == this.takeOver) {
                    return;
                }
                throw new IllegalStateException();
            }
            this.takeOver = takeOver;
        }
    }
}
