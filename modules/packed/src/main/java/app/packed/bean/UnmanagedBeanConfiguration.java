package app.packed.bean;

public non-sealed class UnmanagedBeanConfiguration<T> extends BeanConfiguration<T> {

    public UnmanagedBeanConfiguration() {}
    
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
