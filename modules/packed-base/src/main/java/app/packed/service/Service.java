package app.packed.service;

import app.packed.base.AttributedElement;
import app.packed.base.Key;
import app.packed.config.ConfigSite;

/**
 * An immutable description of a service provided by an injector or similar entity.
 * 
 * @apiNote In the future, if the Java language permits, {@link Service} may become a {@code sealed} interface, which
 *          would prohibit subclassing except by explicitly permitted types.
 */
public interface Service extends AttributedElement {

    /**
     * Returns whether or not the service being provided is a constant.
     * <p>
     * Constant services can always be cached.
     * 
     * @return whether or not the service being provided is a constant
     */
    default boolean isConstant() {
        return false;
    }

    /**
     * Returns the configuration site of this service.
     * 
     * @return the configuration site of this service
     */
    // I think this is an attribute...
    ConfigSite configSite();

    /**
     * Returns the key that the service is registered with.
     *
     * @return the key that the service is registered with
     */
    Key<?> key();
}
//Skal omnavngives til service hvis vi får en context...
//Vi kalder den service ligesom component...
// Component component(); <-- the component the service belongs to..
