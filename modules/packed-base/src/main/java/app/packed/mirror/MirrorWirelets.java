package app.packed.mirror;

import app.packed.component.Wirelet;

// Wirelet retainModelsAtRuntime();
// Wirelet allDetails();

// Kan ogsaa vaere den bare skal paa BuildWirelets...
// Vi kan jo ikke ligesom specificere det paa runtime. Eftersom
// det jo er en del af injection modellen...
// Taenker vi
public class MirrorWirelets {

    public static Wirelet retainModelsAtRuntime() {
        throw new UnsupportedOperationException();
    }
    
    // Problemet er lidt alle de relations typer...
    @SafeVarargs
    public static Wirelet retainModelsAtRuntime(Class<? extends Mirror>... modelTypes) {
        throw new UnsupportedOperationException();
    }
}
// retainDetailedConfigSites() full stack trace