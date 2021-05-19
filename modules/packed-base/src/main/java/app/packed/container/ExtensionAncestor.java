package app.packed.container;

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

public interface ExtensionAncestor<E> {

    E get();

    /**
     * Returns true if an ancestor is present and the ancestor is a direct parent.
     * 
     * @return true if parent, false if non-parent or empty
     */
    boolean isParent();

    boolean isPresent();

    boolean isSameApplication();

    boolean isStronglyWired();

    void onUninstall(Runnable r);// Ved ikke om det er det rigtige sted...

    static <T> ExtensionAncestor<T> empty() {
        return PackedExtensionAncestor.missing();
    }
}
/// Hvordan klare vi f.eks. at specificere at en host kun exportere Foo og Bar...
/// 