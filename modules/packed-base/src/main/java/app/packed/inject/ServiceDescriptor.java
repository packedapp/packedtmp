package app.packed.inject;

import java.util.Optional;

import app.packed.config.ConfigSite;
import app.packed.util.Key;

/** An immutable description of a service provided by an injector or similar entity. */
// Skal omnavngives til service hvis vi får en context...
public interface ServiceDescriptor /* extends Taggable */ {

    /**
     * Returns the configuration site of this service.
     * 
     * @return the configuration site of this service
     */
    ConfigSite configSite();

    /**
     * Returns the optional description of this service.
     *
     * @return the optional description of this service
     * @see ComponentServiceConfiguration#setDescription(String)
     */
    Optional<String> description();

    /**
     * Returns the key that the service is registered with.
     *
     * @return the key that the service is registered with
     * @see ComponentServiceConfiguration#as(Key)
     */
    Key<?> key();
}

// ComponentPath() <- The path to the component that defines the service...
//// What if we use wirelets????
//// Same problem as with ServiceRequest....
////

// Optional<Class<?>> implementation(); Does not work for @Provides...
// Ideen var at vi ville bruge det til at skrive XZY.
// Men hvis vi nu ikke