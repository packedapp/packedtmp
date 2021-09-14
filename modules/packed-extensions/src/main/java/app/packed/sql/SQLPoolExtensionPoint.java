package app.packed.sql;

public interface SQLPoolExtensionPoint {
    
    void beforeFirstRequest();
    
    void startPool();
    
    void stopPool();
    
    
    // Called by the extension when it is done with it
    // Problemet er med shutdown af dependencies laengere nede.
    // Som jeg ser da blive lukket ned
    void afterLastRequest();
}
