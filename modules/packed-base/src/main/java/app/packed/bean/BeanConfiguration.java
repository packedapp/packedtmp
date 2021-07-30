package app.packed.bean;

import app.packed.component.ComponentConfiguration;

@SuppressWarnings("rawtypes")
public abstract sealed class BeanConfiguration<T> extends ComponentConfiguration permits ApplicationBeanConfiguration, ManagedBeanConfiguration, UnmanagedBeanConfiguration {

    /**
     * This method can be overridden to return a subclass of bean mirror.
     * 
     * {@inheritDoc}
     */
    @Override
    protected BeanMirror mirror() {
        throw new UnsupportedOperationException();
    }
    
    public abstract BeanKind kind();
}
