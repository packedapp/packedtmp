package app.packed.hooks.sandbox;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Member;
import java.util.Optional;

import app.packed.base.InvalidKeyException;
import app.packed.base.Key;
import app.packed.extension.Extension;

/**
 * This hook can be activated in 3 different ways
 */
// Maaske bare ServiceHook
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ServiceHook {

    /** Bootstrap classes for this hook. */
    Class<? extends ServiceHook.Bootstrap>[] bootstrap();

    /** Any extension this hook is part of. */
    Class<? extends Extension> extension();

    /** Any annotations that activates the method hook. */
    Class<?>[] matchesClass() default {};

    static class Bootstrap {

        public final Class<?> declaringClass() {
            return Class.class;
        }

        /**
         * If the requesting party is part of an extension, returns the extension. Otherwise returns empty.
         * <p>
         * Any extension returned by this method is guaranteed to have {@link ServiceHook#extension()} as a (direct) dependency.
         * 
         * @return the extension using the hook, or empty if user code
         * @see #$failForOtherExtensions()
         */
        public final Optional<Class<? extends Extension>> extension() {
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

        /**
         * Creates a ne
         * 
         * @return a key
         * @throws InvalidKeyException
         *             if a valid key could not be created
         * @see Key#convertField(java.lang.reflect.Field)
         * @see Key#convertParameter(java.lang.reflect.Parameter)
         * @see Key#convertTypeLiteral(app.packed.base.TypeToken)
         */
        // Hmm, hvordan klare vi Provider, Lazy, Optional her...
        //// Det er jo noget vi fjerner foerst... Eller goer vi det i keyen?
        //// Maaske vi goere det i key'en
        final Key<?> key() {
            throw new UnsupportedOperationException();
        }

        final Optional<Member> member() {
            throw new UnsupportedOperationException();
        }

        public final Module module() {
            throw new UnsupportedOperationException();
        }

        // Altsaa hvis klassen er parameterized... Gaar jeg udfra man gerne vil bruge den...
        static void $allowParameterized() {}

        static void $failForField() {}

        // Only user code can use the service...
        // IDK if it makes sense
        //// Maybe have an error message???
        //// For example, @Provide on an extension... A user service???
        //// IDK
        static void $failForOtherExtensions() {}

        static void $failForParameter() {}

        static void $failForTypeVariable() {}

        // ignore|fail on Provider

        // Alternativt kan man specificere en masse
        static void $ignoreNullable() {}

        static void $ignoreOptional() {}
    }
}
// Til noeds kan vi klare super klasser...
// Og saa det altid den dybeste klasse der matcher foerst...
// Kunne f.eks. bruges til Wirelet...
