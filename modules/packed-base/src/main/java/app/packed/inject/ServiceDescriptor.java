package app.packed.inject;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import app.packed.config.ConfigurationSite;
import app.packed.util.Key;
import app.packed.util.Nullable;
import app.packed.util.Taggable;

class CopyOfConfiguration implements ServiceDescriptor {

    private final ConfigurationSite configurationSite;

    private final @Nullable String description;

    private final Key<?> key;

    private final Set<String> tags;

    CopyOfConfiguration(ServiceConfiguration<?> bne) {
        this.key = bne.getKey();
        this.tags = Set.copyOf(bne.tags());
        this.configurationSite = bne.configurationSite();
        this.description = bne.getDescription();
    }

    /** {@inheritDoc} */
    @Override
    public ConfigurationSite configurationSite() {
        return configurationSite;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    /** {@inheritDoc} */
    @Override
    public Key<?> key() {
        return key;
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> tags() {
        return tags;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[Key = " + key().toStringSimple());
        // sb.append(", instantionMode = " + getInstantiationMode());
        if (!tags.isEmpty()) {
            sb.append(", tags = " + tags);
        }
        sb.append("]");
        return sb.toString();
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
    public ConfigurationSite configurationSite() {
        return configuration.configurationSite();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> description() {
        return Optional.ofNullable(configuration.getDescription());
    }

    /** {@inheritDoc} */
    @Override
    public Key<?> key() {
        return configuration.getKey();
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> tags() {
        return Collections.unmodifiableSet(configuration.tags());
    }
}

/** An immutable description of a service. */
public interface ServiceDescriptor extends Taggable {

    /**
     * Returns the configuration site of this service.
     * 
     * @return the configuration site of this service
     */
    ConfigurationSite configurationSite();

    /**
     * Returns the description of this service.
     *
     * @return the description of this service
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

    static ServiceDescriptor copyOf(ServiceConfiguration<?> configuration) {
        return new CopyOfConfiguration(configuration);
    }

    static void d(Injector i) {
        i.getService(String.class).configurationSite().print();
    }

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