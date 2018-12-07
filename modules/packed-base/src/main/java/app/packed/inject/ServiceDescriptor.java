package app.packed.inject;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Set;

import app.packed.util.ConfigurationSite;
import app.packed.util.Key;
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

    /**
     * Returns an unmodifiable view of the specified service configuration. Operations on the returned descriptor "read
     * through" to the specified configuration.
     * 
     * @param configuration
     *            the service configuration to create an adaptor for
     * @return the read through descriptor
     */
    static ServiceDescriptor of(ServiceConfiguration<?> configuration) {
        return new ImmutableServiceDescriptorAdaptor(configuration);
    }
}

class ImmutableServiceDescriptorAdaptor implements ServiceDescriptor {

    /** The configuration we read through to. */
    private final ServiceConfiguration<?> configuration;

    /**
     * @param configuration
     */
    public ImmutableServiceDescriptorAdaptor(ServiceConfiguration<?> configuration) {
        this.configuration = requireNonNull(configuration, "configuration is null");
    }

    /** {@inheritDoc} */
    @Override
    public BindingMode getBindingMode() {
        return configuration.getBindingMode();
    }

    /** {@inheritDoc} */
    @Override
    public ConfigurationSite getConfigurationSite() {
        return configuration.getConfigurationSite();
    }

    /** {@inheritDoc} */
    @Override
    public @Nullable String getDescription() {
        return configuration.getDescription();
    }

    /** {@inheritDoc} */
    @Override
    public Key<?> getKey() {
        return configuration.getKey();
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> tags() {
        return Collections.unmodifiableSet(configuration.tags());
    }
}