package app.packed.container;

import app.packed.base.NamespacePath;
import app.packed.component.ComponentConfiguration;

/**
 * A configuration object for an {@link Extensor}.
 * <p>
 * An instance of this class is returned when installing an Extensor via
 * 
 * ExtensionContext or
 * 
 * Extension.
 * 
 */
public final class ExtensorConfiguration extends ComponentConfiguration {

    /** {@inheritDoc} */
    @Override
    public NamespacePath path() {
        return super.path();
    }

    // Nu skal de sgu nok hedde .ServiceExtension alligevel paa runtime
}
