package app.packed.bean;

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
