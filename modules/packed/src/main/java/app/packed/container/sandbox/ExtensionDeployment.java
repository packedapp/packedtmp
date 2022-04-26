package app.packed.container.sandbox;

import app.packed.container.Extension;

// Deployment of features...
// isStrongly connected

//// Ved ikke om vi nogensinde boer have staerke referencer
//// Paa instans niveau ja. 
public abstract class ExtensionDeployment<E extends Extension<E>> {
    
    protected void undeploy(boolean forUpgrade) {
        
    }
}
