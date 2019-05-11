package packed.internal.inject.builder;

import static java.util.Objects.requireNonNull;

import java.util.List;

import app.packed.bundle.WiringOption;
import packed.internal.config.site.InternalConfigurationSite;

public abstract class ProvideAll {

    /** The wiring options used when creating this configuration. */
    final List<WiringOption> options;

    /** The configuration site of this object. */
    final InternalConfigurationSite configurationSite;

    protected ProvideAll(InternalConfigurationSite configurationSite, WiringOption... options) {
        this.configurationSite = requireNonNull(configurationSite);
        this.options = List.of(options);
    }

}
