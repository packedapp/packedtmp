package app.packed.bean;

// ExtensionManagedBean?
public class ManagedBeanConfiguration<T> extends BeanConfiguration<T> {

    public ManagedBeanConfiguration(BeanMaker<T> handle) {
        super(handle);
        
    }
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
