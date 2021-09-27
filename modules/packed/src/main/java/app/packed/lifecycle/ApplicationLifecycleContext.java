package app.packed.lifecycle;

public interface ApplicationLifecycleContext {
    
    void awaitIfPresent(WaitToken token);
    
    static interface WaitToken {}
    
    // Gemmer den i en statisk felt...
    //
    static interface TokenHolder {
        WaitToken wt();
        void release();
    }
}
