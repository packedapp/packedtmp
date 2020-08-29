package app.packed.service;

import app.packed.base.AttributedElement;
import app.packed.base.AttributeMap;
import app.packed.base.Key;
import app.packed.config.ConfigSite;

/** An immutable description of a service provided by an injector or similar entity. */
// Skal omnavngives til service hvis vi får en context...
public interface ServiceDescriptor extends AttributedElement {

    @Override
    default AttributeMap attributes() {
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
