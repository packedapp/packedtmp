package app.packed.application;

import java.util.function.Supplier;

import app.packed.bean.ContainerBeanConfiguration;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionSupport;

// Cannot use both @app.packed.application.Main and the CLI extension at the same time.

// Taenker vi godt vil have en extension...

// Engang ville vi vil gerne droppe den. Hoved grunden er at applikationen godt vil bestemme om entry points'ene.
// Altsaa vi skal jo ikke goere noget specifikt fra assemblien.
// Det er jo dem der deployer/mapper applikationen som siger hvad der skal ske. Dvs. 
// De kan ikke bestemme at der skal installeres en extension
public class EntryPointExtension extends Extension {

    public void setShutdownStrategy(Supplier<Throwable> maker) {
        // Ideen er lidt at sige hvad sker der paa runtime.
        // Hvis vi shutter down... Default er at der bliver
        // Sat en cancellation exception som reaason
        // CancellationException
        // Men ved ikke om vi skal have en special exception istedet for???
        // En checked istedet ligesom TimeoutException
    }

    public <T extends Runnable> ContainerBeanConfiguration<?> mainBeanInstance(T runnable) {
        // Hehe, tag den, hvad goer vi her, den ene er en bean
        // Den anden er en function.
        // Tror ikke vi skal have begge dele
        throw new UnsupportedOperationException();
    }

    public void main(Runnable runnable) {
        // Det her er en function
    }

    public class Support extends ExtensionSupport {

        // Kan kun bliver
        /**
         * 
         * @throws IllegalStateException
         *             if multiple different extensions tries to add entry points
         */
        public void manage() {
            // Maaske automanager vi bare ting, naar bruger den her Subtension...
            // Er ikke sikker paa der er nogen grund til at aktivere den, hvis
            // man ikke har intension om at bruge den
        }

        // Ideen er at man kan wrappe sin entrypoint wirelet..
        // Eller hva...
        // Du faar CLI.wirelet ind som kan noget med sine hooks
//        static Wirelet wrap(Wirelet w) {
//            return w;
//        }
    }
}
