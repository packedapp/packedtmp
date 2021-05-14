package app.packed.container;

import app.packed.component.ComponentConfiguration;

public abstract class AbstractContainerConfiguration extends ComponentConfiguration {
   
    /**
     * Creates a new base component configuration.
     * 
     * @param context
     *            the configuration context for the component
     */
    public AbstractContainerConfiguration(ComponentConfigurationContext context) {
        super(context);
    }


}
