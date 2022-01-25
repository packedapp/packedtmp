package app.packed.bean.archive;

import app.packed.bean.BeanMaker;
import app.packed.bean.InstanceBeanConfiguration;

// ExtensionManagedBean?
public class ManagedBeanConfiguration<T> extends InstanceBeanConfiguration<T> {

    public ManagedBeanConfiguration(BeanMaker<T> handle) {
        super(handle);
    }

    /** {@inheritDoc} */
    @Override
    public ManagedBeanConfiguration<T> named(String name) {
        super.named(name);
        return this;
    }
}
