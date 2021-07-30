package app.packed.bean;

public non-sealed class ManagedBeanConfiguration<T> extends BeanConfiguration<T> {

    /** {@inheritDoc} */
    @Override
    public final BeanKind kind() {
        return BeanKind.MANAGED;
    }

    /** {@inheritDoc} */
    @Override
    public ManagedBeanConfiguration<T> named(String name) {
        super.named(name);
        return this;
    }
}
