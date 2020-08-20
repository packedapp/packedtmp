package app.packed.service;

import app.packed.base.AttributeHolder;
import app.packed.base.AttributeSet;
import app.packed.base.Key;
import app.packed.config.ConfigSite;

/** An immutable description of a service provided by an injector or similar entity. */
// Skal omnavngives til service hvis vi får en context...
public interface ServiceDescriptor extends AttributeHolder {

    @Override
    default AttributeSet attributes() {
        throw new UnsupportedOperationException();
    }

    // For example, if a SingletonComponent is exposed as a service.
    // It will always be constant. For
    default boolean isConstant() {
        return false;
    }

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
