package app.packed.artifact;

import app.packed.base.Contract;
import app.packed.container.Bundle;
import app.packed.container.BundleDescriptor;
import packed.internal.moduleaccess.AppPackedArtifactAccess;
import packed.internal.moduleaccess.ModuleAccess;

/**
 * The various types of build process goals available in Packed.
 */
public enum BuildGoal {

    /** Create an artifact such as {@link App}. */
    ARTIFACT,

    /** Create an {@link ArtifactImage}. */
    IMAGE,

    /** Create a report of some kind, for example a {@link Contract} or a {@link BundleDescriptor}. */
    REPORT,

    /** Verify that a {@link Bundle} is valid. */
    VERIFY;

    static {
        ModuleAccess.initialize(AppPackedArtifactAccess.class, new AppPackedArtifactAccess() {

            /** {@inheritDoc} */
            @Override
            public <T> T newArtifact(ArtifactDriver<T> driver, ArtifactContext context) {
                return driver.newArtifact(context);
            }
        });
    }
}
// Artifact + Wirelets
// Image + Wirelets
// Verify + Wirelets?
// Report

// Artifact ->  ArtifactType | HowFarToGo [Initialized, Starting, Running, Terminated]

// Its not an output because Verify is not an output but absense of a creation exception

// VerificationReport (Is still a report goal, and not a verify goal)