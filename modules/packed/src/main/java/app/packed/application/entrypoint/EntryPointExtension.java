package app.packed.application.entrypoint;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import app.packed.bean.ContainerBeanConfiguration;
import app.packed.extension.Extension;

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

    @Override
    public EntryPointExtensionMirror mirror() {
        return mirrorInitialize(new EntryPointExtensionMirror(tree()));
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

    static class Shared {
        int entryPointCount;
        MethodHandle[] entryPoints;
        Class<? extends Extension<?>> takeOver;
        final List<EntryPointConf> entrypoints = new ArrayList<>();

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

    static class EntryPointConf {

    }
}
