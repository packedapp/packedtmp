package app.packed.application.entrypoint;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import app.packed.bean.ContainerBeanConfiguration;
import app.packed.bean.hooks.BeanMethod;
import app.packed.extension.Extension;
import app.packed.inject.service.ServiceExtension;
import packed.internal.application.EntryPointSetup;
import packed.internal.application.EntryPointSetup.MainThreadOfControl;
import packed.internal.bean.BeanSetup;
import packed.internal.bean.hooks.HookedBeanMethod;

// Cannot use both @app.packed.application.Main and the CLI extension at the same time.
/**
 * An extension that controls any entry points of the an application.
 */
public class EntryPointExtension extends Extension<EntryPointExtension> {

    boolean hasMain;

    Shared shared;

    /* package-private */ EntryPointExtension() {}

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
        return mirrorInitialize(new EntryPointExtensionMirror(tree()));
    }

    @Override
    protected void hookOnBeanMethod(Class<? extends Annotation> annotation, BeanMethod method) {
        MethodHandle mh = method.methodHandle();
        Method m = method.method();
        new EntryPointSupport(this, null).registerEntryPoint(true);
        hasMain = true;

        BeanSetup c = ((HookedBeanMethod) method).scanner.bean;
        c.parent.useExtension(ServiceExtension.class);
        c.application.entryPoints = new EntryPointSetup();

        MainThreadOfControl mc = c.application.entryPoints.mainThread();
        mc.isStatic = Modifier.isStatic(m.getModifiers());
        mc.cs = (BeanSetup) c;
        mc.methodHandle = mh;

//        oldOperation().useMirror(() -> new EntryPointMirror(0));
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

    static class Shared {
        int entryPointCount;
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
