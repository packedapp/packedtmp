package app.packed.container;

import app.packed.inject.Factory;

// Taenker den opfoere sig som enhver anden component...
// Og det primaert 
public abstract class RuntimeExtension<T extends Extension> {

    static {

    }

}

class ExtensionNew {

    // Altsaa den skal jo naesten vaere single instance
    // Vi kan jo ikke tage en extension i constructeren hvis den er runtime

    void runtimeInstallInstance(RuntimeExtension<?> instance) {}
    void runtimeInstall(Factory<? extends RuntimeExtension<?>> factory) {}
    void runtimeInstall(Class<? extends RuntimeExtension<?>> implementation) {}
}

//--------------- Hvor mange kan man have?
// Saa mange man har lyst til...
// Saa kan man jo optional injecte dem

//Maaaske stopper vi application code foer extension code

//---------------- Abstract vs Interface
// ... Det er fint den er abstract
// Bliver der maaske lavet 10.000 extensions i alt i Packeds livstid..