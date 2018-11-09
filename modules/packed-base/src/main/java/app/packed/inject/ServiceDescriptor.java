package app.packed.inject;

import app.packed.util.ConfigurationSite;
import app.packed.util.Nullable;
import app.packed.util.Taggable;

/** An immutable description of a service. */
public interface ServiceDescriptor extends Taggable {

    /**
     * Returns the binding mode of the service.
     *
     * @return the binding mode of the service
     */
    BindingMode getBindingMode();

    /**
     * Returns the configuration site of the service.
     * 
     * @return the configuration site of the service
     */
    ConfigurationSite getConfigurationSite();

    /**
     * Returns the description of this service. Or null if no description has been set.
     *
     * @return the description of this service
     * @see ServiceConfiguration#setDescription(String)
     */
    @Nullable
    String getDescription();

    /**
     * Returns the key that the service is registered under.
     *
     * @return the key that the service is registered under
     * @see ServiceConfiguration#as(Key)
     */
    Key<?> getKey();
}
