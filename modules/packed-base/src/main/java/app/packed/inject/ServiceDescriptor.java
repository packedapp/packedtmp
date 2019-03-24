package app.packed.inject;

import java.util.Optional;

import app.packed.config.ConfigSite;
import app.packed.util.Key;
import app.packed.util.Taggable;

/** An immutable description of a service provided by an injector or similar entity. */
// Skal omnavngives til service hvis vi får en context...
// Skal
public interface ServiceDescriptor extends Taggable {

    /**
     * Returns the configuration site of this service.
     * 
     * @return the configuration site of this service
     */
    ConfigSite configurationSite();

    /**
     * Returns the optional description of this service.
     *
     * @return the optional description of this service
     * @see ServiceConfiguration#setDescription(String)
     */
    Optional<String> description();

    /**
     * Returns the key that the service is registered with.
     *
     * @return the key that the service is registered with
     * @see ServiceConfiguration#as(Key)
     */
    Key<?> key();
}
// Optional<Class<?>> implementation(); Does not work for @Provides...