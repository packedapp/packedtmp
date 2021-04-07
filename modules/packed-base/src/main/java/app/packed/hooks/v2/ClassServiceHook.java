package app.packed.hooks.v2;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Member;
import java.util.Optional;

import app.packed.base.InvalidKeyException;
import app.packed.base.Key;
import app.packed.container.Extension;

/**
 * This hook can be activated in 3 different ways
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ClassServiceHook {

    /** Bootstrap classes for this hook. */
    Class<? extends ClassServiceHook.Bootstrap>[] bootstrap();

    /** Any extension this hook is part of. */
    Class<? extends Extension> extension();

    /** Any annotations that activates the method hook. */
    Class<?>[] matchesClass() default {};

    static class Bootstrap {

        public final Class<?> declaringClass() {
            return Class.class;
        }

        /**
         * Creates a ne
         * 
         * @return a key
         * @throws InvalidKeyException
         *             if a valid key could not be created
         * @see Key#convertField(java.lang.reflect.Field)
         * @see Key#convertParameter(app.packed.base.Parameter)
         * @see Key#convertTypeLiteral(app.packed.base.TypeToken)
         */
        // Hmm, hvordan klare vi Provider, Lazy, Optional her...
        //// Det er jo noget vi fjerner foerst... Eller goer vi det i keyen?
        //// Maaske vi goere det i key'en
        final Key<?> key() {
            throw new UnsupportedOperationException();
        }

        final boolean isField() {
            return false;
        }

        final boolean isParameter() {
            return false;
        }

        final boolean isTypeVariable() {
            return false;
        }

        final Optional<Member> member() {
            throw new UnsupportedOperationException();
        }

        static void $failForTypeVariable() {}
        static void $failForField() {}
        static void $failForParameter() {}
        
        // Altsaa hvis klassen er parameterized... Gaar jeg udfra man gerne vil bruge den...
        static void $allowParameterized() {}

        // ignore|fail on Provider

        // Alternativt kan man specificere en masse
        static void $ignoreNullable() {}

        static void $ignoreOptional() {}
    }
}
// Til noeds kan vi klare super klasser...
// Og saa det altid den dybeste klasse der matcher foerst...
// Kunne f.eks. bruges til Wirelet...
