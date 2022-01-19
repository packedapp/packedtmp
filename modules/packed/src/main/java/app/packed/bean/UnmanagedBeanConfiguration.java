package app.packed.bean;

public class UnmanagedBeanConfiguration<T> extends BeanConfiguration<T> {
    
    /**
     * @param handle
     */
    protected UnmanagedBeanConfiguration(BeanMaker<T> handle) {
        super(handle);
    }

    /** {@inheritDoc} */
    @Override
    public final BeanKind kind() {
        return BeanKind.UNMANAGED;
    }

    /** {@inheritDoc} */
    @Override
    public UnmanagedBeanConfiguration<T> named(String name) {
        super.named(name);
        return this;
    }
}
