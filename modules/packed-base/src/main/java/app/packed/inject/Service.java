package app.packed.inject;

import app.packed.base.AttributedElement;
import app.packed.base.Key;

/**
 * An immutable description of a service.
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
    boolean isConstant();

    /**
     * Returns the key that the service is registered with.
     *
     * @return the key that the service is registered with
     */
    Key<?> key();

    /**
     * Returns the mode of the service.
     * 
     * @return the mode of the service
     */
    default ServiceMode mode() {
        return isConstant() ? ServiceMode.CONSTANT : ServiceMode.PROTOYPE;
    }

    /**
     * Returns the raw type of the service. Actual service instances may be subclasses of the returned type.
     * 
     * @return the raw type of the service
     * @see Key#rawType()
     */
    default Class<?> rawType() {
        return key().rawType();
    }
}
