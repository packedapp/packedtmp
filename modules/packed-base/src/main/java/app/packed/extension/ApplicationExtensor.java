package app.packed.extension;

public abstract class ApplicationExtensor<E extends Extension> extends ExtensionBean<E> {
    
    static void $initializeOnly() {
        // only available doing container initialization... after which it will magically disapper
        // Ville vaere fedt hvis jeg havde skrevet nogle use cases ned
    }
}
