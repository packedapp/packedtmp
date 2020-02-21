package app.packed.container;

import java.util.List;

/**
 * This class can be used to verify that a bundle can be successfully. For example, doing testing.
 */

// Constraints.....
// Does not use Beta APIS....

// For example, that there are circles in the graph is not a constraint. Its always an error.
// Because we cannot create an artifact with a circle.

//Actually you should also be able to verify an image...
//Test that this image does not use BetaStuff...

//ValidatedBundle (sounds like it is always correct)
//If we do allow wirelets, we might want to change it to ArtifactValidator
//Because then we should probably allow images + wirelets
interface BundleValidator {
    // Maaske er det en slags visitor???
    // Det og det og det er wrong...

    // Ville jo vaere rart godt naar man skal rapportere det...
    List<String> errors();

    List<String> info();

    boolean isOK();

    List<String> warnings();

    /**
     * Asserts that the specified bundle is valid, failing with {@link AssertionError} if not.
     * 
     * @param bundle
     *            the bundle to validate
     * @throws AssertionError
     *             if the bundle is not valid
     */
    static void assertValid(Bundle bundle) {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks that the specified bundle is valid. Returning <code>true</code> if it is valid, otherwise <code>false</code>.
     * 
     * @param bundle
     *            the bundle to validate
     * @return whether or not the specified bundle is valid
     */
    static boolean isValid(Bundle bundle) {
        throw new UnsupportedOperationException();
    }

    static BundleValidator validate(Bundle bundle) {
        throw new UnsupportedOperationException();
    }

    // Min eneste anke mod denne er
    // Man skal vel kunne specificere alle disse ting via wirelets ogsaa....
    // F.eks. checkNonBetaAPIs.... vil vi godt kunne bruge andre steder...
    // Eller?
    interface Builder {

        @SuppressWarnings("exports")
        // Stuff to check

        // failWith(MethodHandles.Lookup lookup, Class<? extends Exception>);

        BundleValidator verify(Bundle b);
    }
    // Can throw AssertionException (Maybe its the default)
    // Maybe write stuff to a file
}

// Constraint... <- Is It a class?? No that would be too much...
// Constraints works with Features?????? Yeah why not...
// A.la. QueryLanguage....

// Static Validation <- works on a class level
// Constraints works on a Container/Artifact Level....

// ConstraintListenerWirelet.... 

// Er det for en artifact????
// Eller for flere????

//Syntes det her er en rapport

//Represents a failure. Failures are similar to exceptions but carry less information (only a message, a description and a cause) so they can be used in a wider scope than just the JVM where the exception failed.
//https://docs.gradle.org/current/javadoc/org/gradle/tooling/Failure.html

// No need for a stack trace... does not really make sense...

// Also thrown on create....
