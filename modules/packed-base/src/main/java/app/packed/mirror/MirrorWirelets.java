package app.packed.mirror;

import app.packed.component.Wirelet;

// Wirelet retainModelsAtRuntime();
// Wirelet allDetails();

// Naar vi har defineres dem der er relevante, kan jeg sagtens forstille mig de ryger paa BuildWirelets...

// Vi kan jo ikke ligesom specificere det paa runtime. Eftersom
// det jo er en del af injection modellen...
// Taenker vi
public class MirrorWirelets {
    
    // Taenker vi ikke noedvendigvis behoever specifire den ved build???
    // IDK.. Maaske. Jeg har svaert ved at se situation hvor det giver mening
    // at specificere kun ved runtime
    
    // Taenker vi maa have nogle levels...
    // Eller gemmer vi alt saaledes at vi kan calcure ting

    // Altsaa isaer for extensions kan jeg se det er et problem...
    // Meget af det bliver jo lazy computed...
    // Taenker ikke vi supporter det fra starten af
    
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