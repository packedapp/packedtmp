package app.packed.service;

import app.packed.base.Key;
import app.packed.config.ConfigSite;

/** An immutable description of a service provided by an injector or similar entity. */
// Skal omnavngives til service hvis vi får en context...
public interface ServiceDescriptor {

    /**
     * Returns the configuration site of this service.
     * 
     * @return the configuration site of this service
     */
    ConfigSite configSite();

    /**
     * Returns the key that the service is registered with.
     *
     * @return the key that the service is registered with
     */
    Key<?> key();
}
// Det er jo en slags feature provided af en Component, saa maaske
// extends ServiceFeature

// Som saa ville have en ConfigSite + ComponentPath....

// ComponentPath() <- The path to the component that defines the service...
//// What if we use wirelets????
//// Same problem as with ServiceRequest....
// ComponentPath paa containeren vil jeg mene..

// Optional<Class<?>> implementation(); Does not work for @Provides...
// Ideen var at vi ville bruge det til at skrive XZY.
// Men hvis vi nu ikke