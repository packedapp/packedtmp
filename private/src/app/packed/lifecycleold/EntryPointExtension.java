package app.packed.lifecycleold;

import app.packed.bean.ApplicationBeanConfiguration;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionSupport;

// Taenker vi godt vil have en extension...

// Vi vil gerne droppe den. Hoved grunden er at applikationen godt vil bestemme om entry points'ene.
// Altsaa vi skal jo ikke goere noget specifikt fra assemblien.
// Det er jo dem der deployer/mapper applikationen som siger hvad der skal ske. Dvs. 
// De kan ikke bestemme at der skal installeres en extension
public class EntryPointExtension extends Extension {

    public <T extends Runnable> ApplicationBeanConfiguration<?> mainBeanInstance(T runnable) {
        // Hehe, tag den, hvad goer vi her, den ene er en bean
        // Den anden er en function.
        // Tror ikke vi skal have begge dele
        throw new UnsupportedOperationException();
    }

    public void main(Runnable runnable) {
        // Det her er en function
    }

    public class EntryPointExtensionSupport extends ExtensionSupport {

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
    }
}
