package app.packed.extension;

public abstract class ApplicationExtensor<E extends Extension> extends Extensor<E> {
    
    static void $initializeOnly() {
        // only available doing container initialization... after which it will magically disapper
    }
}
