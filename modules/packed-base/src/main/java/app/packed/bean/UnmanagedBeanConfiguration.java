package app.packed.bean;

public non-sealed class UnmanagedBeanConfiguration<T> extends BeanConfiguration<T> {

    /** {@inheritDoc} */
    @Override
    public UnmanagedBeanConfiguration<T> named(String name) {
        super.named(name);
        return this;
    }

    @Override
    public final BeanKind kind() {
        return BeanKind.UNMANAGED;
    }
}
