package app.packed.extension;

public interface ExtensionBeanSearch<T> {

    void installNew();
    
    T ancestor();
    
    boolean isMissing();
    boolean isPresent();
    
    // true if present
    boolean inheritIfPresent();
}
