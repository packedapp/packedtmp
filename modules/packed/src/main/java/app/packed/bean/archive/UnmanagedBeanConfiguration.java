package app.packed.bean.archive;

import app.packed.bean.BeanMaker;
import app.packed.bean.InstanceBeanConfiguration;

public class UnmanagedBeanConfiguration<T> extends InstanceBeanConfiguration<T> {
    
    /**
     * @param handle
     */
    protected UnmanagedBeanConfiguration(BeanMaker<T> handle) {
        super(handle);
    }

    /** {@inheritDoc} */
    @Override
    public UnmanagedBeanConfiguration<T> named(String name) {
        super.named(name);
        return this;
    }
}
