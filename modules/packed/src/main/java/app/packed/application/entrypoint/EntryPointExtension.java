package app.packed.application.entrypoint;

import java.lang.invoke.MethodHandle;
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

    protected void onNew() {
        shared = configuration().isApplicationRoot() ? new Shared() : applicationTree().root().shared;
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

    @Override
    protected void onClose() {
        if (isApplicationRoot()) {
            // Her installere vi MethodHandles der bliver shared, taenker det er bedre end at faa injected
            // extensions'ene
            
            // install
            // provide (visible i mxxz;ias:"?aZ.a:n  aZz¸ m  nl jj m ,m   ;n .n ≥ en container)
            // provideShared (container + subcontainers)
            shareInstance(new MethodHandle[0]);
        }
        super.onClose();
    }

    @Override
    public EntryPointExtensionMirror mirror() {
        return mirrorInitialize(new EntryPointExtensionMirror(tree()));
    }

    public void setShutdownStrategy(Supplier<Throwable> maker) {
        // Ideen er lidt at sige hvad sker der paa runtime.
        // Hvis vi shutter down... Default er at der bliver
        // Sat en cancellation exception som reaason
        // CancellationException
        // Men ved ikke om vi skal have en special exception istedet for???
        // En checked istedet ligesom TimeoutException
    }

    static class Shared {
        MethodHandle[] entryPoints;
        Class<? extends Extension<?>> takeOver;
        int entryPointCount;
        void takeOver(Class<? extends Extension<?>> takeOver) {
            if (this.takeOver != null) {

            } else if (entryPointCount > 0) { // has main

            }
            this.takeOver = takeOver;
        }
    }
}
//Engang ville vi vil gerne droppe den. Hoved grunden er at applikationen godt vil bestemme om entry points'ene.
//Altsaa vi skal jo ikke goere noget specifikt fra assemblien.
//Det er jo dem der deployer/mapper applikationen som siger hvad der skal ske. Dvs. 
//De kan ikke bestemme at der skal installeres en extension
