package app.packed.extension;

public interface ExtensorConnection<T> {

    void installNew();
    
    T ancestor();
    
    boolean isMissing();
    boolean isPresent();
    
    // true if present
    boolean inheritIfPresent();
}
