package app.packed.application.entrypoint;

import app.packed.bean.ApplicationBeanConfiguration;
import app.packed.extension.Extension;

// Taenker vi godt vil have en extension...
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

    public class Sub extends Subtension {

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
