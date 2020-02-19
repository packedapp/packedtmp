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
final class BundleVerifier {

    private BundleVerifier() {}

    static VerifierResult evaluate(Bundle bundle) {
        throw new UnsupportedOperationException();
    }

    static void verify(Bundle bundle) {
        throw new UnsupportedOperationException();
    }

    static boolean tryVerify(Bundle bundle) {
        throw new UnsupportedOperationException();
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
interface VerifierResult {
    // Maaske er det en slags visitor???
    // Det og det og det er wrong...

    // Ville jo vaere rart godt naar man skal rapportere det...

    boolean isOK();

    List<String> errors();

    List<String> warnings();

    List<String> info();
}

//Represents a failure. Failures are similar to exceptions but carry less information (only a message, a description and a cause) so they can be used in a wider scope than just the JVM where the exception failed.
//https://docs.gradle.org/current/javadoc/org/gradle/tooling/Failure.html

// No need for a stack trace... does not really make sense...

// Also thrown on create....
class VerificationException {

}