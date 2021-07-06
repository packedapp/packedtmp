package app.packed.extension.old;

import app.packed.extension.ExtensionMember;
import packed.internal.container.PackedExtensionAncestor;

// Extension <same app-parent> Extension
// Extension <same build-parent> Extension
// Extension <same build-ancestor> Extension
// RuntimeExtension <other build-parent> Extension
// RuntimeExtension <other build-ancestor> Extension

// Can any wirelets come between us...

// Behoever ikke blive injected men er metoder paa ExtensionConfiguration/Extension
// Ogsaa for hooks... Vi kan alligevel ikke slippe 100% for noget type hullumhej check...

/**
 * 
 */
// Er det en relation? Nej det er kun parent
// Kan vi bruge den til andet end extensions??? Tjahh. jo da men.... har vi brug for den andet steds

// Man kan ogsaa injecte den.. Saa er det altid ancestor...
// Kan smide den op som Extension.AncestorRelation...

// Maaske kalde den Connection????
// Relation er brugt i mirror api'en men det her er lidt mere aktivt.

// ExtensionConection...
// connectToParent()
public interface ExtensionBeanConnection<E extends ExtensionMember<?>> {

    default void inherit() {}

    E instance();

    boolean isInSameApplication();

    /**
     * Returns true if the ancestor is a direct parent, otherwise false.
     * 
     * @return true if parent, false if non-parent or empty
     */
    boolean isParent();

    boolean isStronglyWired();

    void onUninstall(Runnable r);// Ved ikke om det er det rigtige sted...

    static <T extends ExtensionMember<?>> ExtensionBeanConnection<T> empty() {
        return PackedExtensionAncestor.missing();
    }
}
/// Hvordan klare vi f.eks. at specificere at en host kun exportere Foo og Bar...
/// 