package app.packed.bean.archive;

import app.packed.bean.BeanDriver;
import app.packed.bean.InstanceBeanConfiguration;

// ExtensionManagedBean?
public class ManagedBeanConfiguration<T> extends InstanceBeanConfiguration<T> {

    public ManagedBeanConfiguration(BeanDriver<T> handle) {
        super(handle);
    }

    /** {@inheritDoc} */
    @Override
    public ManagedBeanConfiguration<T> named(String name) {
        super.named(name);
        return this;
    }
}
