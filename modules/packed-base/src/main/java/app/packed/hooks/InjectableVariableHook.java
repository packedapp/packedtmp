package app.packed.hooks;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 *
 */
// Kan man styre tidspunkt med felter???
// Det taenker jeg ikke. Det er altid ved instantiering...
public @interface InjectableVariableHook {

    Class<? extends Annotation>[] annotation() default {};

    Class<?>[] exactClass() default {};

    // Altsaa taenker lidt det giver sig selv om typen er parameterized
    // Hvis den er parameterizered er man jo interesset i at extract den praecise type
    Class<?>[] rawType() default {};

    abstract class Bootstrap {
        
        public final Type parameterizedType() {
            throw new UnsupportedOperationException();
        }
    }
}
