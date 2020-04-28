package packed.internal.artifact;

import app.packed.analysis.BundleDescriptor;
import app.packed.artifact.App;
import app.packed.artifact.SystemImage;
import app.packed.base.Contract;
import app.packed.container.Bundle;

/**
 * The various types of build process goals available in Packed.
 */

// Skal vi overhoved have denne? Hvor kan man faa en instans af den
// Skal vi inteagere paa andre maader. // F.eks. onImage(), onArtifact(), onReport();

//was BuildGoal
// BuildOutput...

//Assemble and Analyze
//Assemble and create Artifact
//Assemble and create Image

//AssembleType, AssembleAnd[Analyze, CreateArtifact, CreateImage]

//AssembleReason

// Hvad med pod??
// Hvad hvis tilfoerjer en component til en artifact....

// Hvad hvis det bare er en komponent???

public enum AssembleTarget {

    /** Create a report of some kind, for example a {@link Contract} or a {@link BundleDescriptor}. */
    // May throw exceptions
    // Will never initialize anything... What about sidecar???
    // I think we need to instantiate them, if they do some kind of initialization
    // Yes, they might report, for example, 3 Gets with same path

    // Analyzes but does not instantiate...
    // Typically creates a report of some kind, or fails by throwing an exception
    ANALYSIS,

    /** Create a new artifact such as an instance of {@link App}. */
    ARTIFACT,

    // Artifact_Initializing Artifact_Executing...
    // Managed Artifact, Unmanaged Artifact....

    /** Create a new {@link SystemImage} from a {@link Bundle} (or an existing image). */
    IMAGE;

}

// Assemblies are self-describing

// Forskellen på verify og report var at verify ikke ville smide en creation exception....
// Men tror det er ligegyldigt for brugere... De skal lave det samme arbejde.
// Saa i virkeligheden ville det bare vaere til besvaer for dem
// Det er jo primaerkt taenkt til et hint til brugere...

// Brugere skal hellere ikke vide om vi f.eks. har taenkt os at starte med det samme, eller
// kun initializere, lad packed kalde dem.

// Artifact + Wirelets
// Image + Wirelets
// Verify + Wirelets?
// Report

//Vi havde Verify engang, men i virkeligheden er det jo lave en verifications report.
// Og fejl hvis den har errors....

// Artifact ->  ArtifactType | HowFarToGo [Initialized, Starting, Running, Terminated]

// Its not an output because Verify is not an output but absense of a creation exception

// VerificationReport (Is still a report goal, and not a verify goal)

// Artifact -> Class<?> ArtifactType, 
// Image -> WriteNativeImage (automatically if is in native img)

// WriteImage -> Automatic, Never (This is only true if auto write is true for both artifact, image, report  )
