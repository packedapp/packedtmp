package app.packed.bean;

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
