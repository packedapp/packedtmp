package app.packed.buildold;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.ContainerBeanConfiguration;
import app.packed.bean.hooks.usage.BeanOldKind;
import app.packed.component.ComponentConfiguration;
import app.packed.inject.Factory;
import app.packed.inject.service.ProvidableBeanConfiguration;
import packed.internal.component.ComponentSetup;
import packed.internal.invoke.Infuser;

/** Implementation of {@link OldBeanDriver.OtherBeanDriver}. */
@SuppressWarnings({ "unchecked", "rawtypes" })
public final class PackedBeanDriverBinder<T, C extends BeanConfiguration> implements OldBeanDriver.OtherBeanDriver<T, C> {

    /** A {@link BeanOldKind#CONTAINER_BEAN} bean binder. */
    public static final PackedBeanDriverBinder SINGLETON_BEAN_BINDER = PackedBeanDriverBinder.of(MethodHandles.lookup(),
            ProvidableBeanConfiguration.class, BeanOldKind.CONTAINER_BEAN);

//    /** A {@link BeanType#STATIC} bean binder. */
//    public static final PackedBeanDriverBinder STATIC_BEAN_BINDER = PackedBeanDriverBinder.of(MethodHandles.lookup(),
//            ApplicationBeanConfiguration.class, BeanType.STATIC);

    final MethodHandle constructor;

    final BeanOldKind kind;

    public PackedBeanDriverBinder(MethodHandle constructor, BeanOldKind kind) {
        this.kind = requireNonNull(kind);
        this.constructor = constructor;
    }

    public static <T> PackedBeanDriver<ContainerBeanConfiguration<T>> ofSingleton(Class<T> implementation) {
        return PackedBeanDriverBinder.SINGLETON_BEAN_BINDER.bind(implementation);
    }

    public static <T> PackedBeanDriver<ContainerBeanConfiguration<T>> ofSingleton(Factory<?> factory) {
        return PackedBeanDriverBinder.SINGLETON_BEAN_BINDER.bind(factory);
    }

    public static <T> PackedBeanDriver<ContainerBeanConfiguration<T>> ofSingletonInstance(Object instance) {
        return PackedBeanDriverBinder.SINGLETON_BEAN_BINDER.bindInstance(instance);
    }

//    public static <T> PackedBeanDriver<ApplicationBeanConfiguration<T>> ofStatic(Class<?> implementation) {
//        return PackedBeanDriverBinder.STATIC_BEAN_BINDER.bind(implementation);
//    }

    /** {@inheritDoc} */
    public PackedBeanDriver<C> bind(Class<? extends T> implementation) {
        requireNonNull(implementation, "implementation is bull");
        return new PackedBeanDriver(this, implementation, implementation);
    }

    /** {@inheritDoc} */
    public PackedBeanDriver<C> bind(Factory<? extends T> factory) {
        requireNonNull(factory, "factory is bull");
        if (kind == BeanOldKind.FUNCTIONAL_BEAN) {
            throw new UnsupportedOperationException("Cannot bind a factory to a static bean.");
        }
        return new PackedBeanDriver(this, factory.rawType(), factory);
    }

    /** {@inheritDoc} */
    public PackedBeanDriver<C> bindInstance(T instance) {
        requireNonNull(instance, "instance is null");
        if (Class.class.isInstance(instance)) {
            throw new IllegalArgumentException("Cannot bind a Class instance to this method, was " + instance);
        } else if (Factory.class.isInstance(instance)) {
            throw new IllegalArgumentException("Cannot bind a Factory instance to this method, was " + instance);
        } else if (kind != BeanOldKind.CONTAINER_BEAN) {
            throw new UnsupportedOperationException("Can only bind instances to singleton beans, kind = " + kind);
        }
        return new PackedBeanDriver(this, instance.getClass(), instance);
    }

    public BeanOldKind kind() {
        return kind;
    }

    public static <T, C extends BeanConfiguration> PackedBeanDriverBinder<T, C> of(MethodHandles.Lookup caller, Class<? extends C> driverType, BeanOldKind kind) {

        // IDK should we just have a Function<ComponentComposer, T>???
        // Unless we have multiple composer/context objects (which it looks like we wont have)
        // Or we fx support @AttributeProvide... This makes no sense..
        // AttributeProvide could make sense... And then some way to say retain this info at runtime...
        // But maybe this is sidecars instead???

        // Grunden til vi gerne vil have

        // Create an infuser for making a method handle for the component configurations's constructor
        Infuser.Builder builder = Infuser.builder(caller, driverType, ComponentSetup.class);

        // TODO Tror godt vi vil injecte baade wirelets
        // SelectWirelets<?>?? og extension

        MethodHandle constructor = builder.findConstructor(ComponentConfiguration.class, e -> new IllegalArgumentException(e));

        return new PackedBeanDriverBinder(constructor, kind);
    }
}
//public interface Option {
//
///**
// * The component the driver will be a container.
// * <p>
// * A container that is a component cannot be sourced??? Yes It can... It can be the actor system
// * 
// * @return stuff
// * @see ComponentModifier#CONSTANT
// */
//// InstanceComponentDriver automatically sets the source...
////static Option sourceAssignableTo(Class<?> rawType) {
////    throw new UnsupportedOperationException();
////}
//
////
////static Option validateParent(Predicate<? super Component> validator, String msg) {
////    return validateWiring((c, d) -> {
////        if (validator.test(c)) {
////            throw new IllegalArgumentException(msg);
////        }
////    });
////}
////
////static Option validateParentIsContainer() {
////    return validateParent(c -> c.hasModifier(ComponentModifier.CONTAINER), "This component can only be wired to a container");
////}
//
//// The parent + the driver
////
////
/////**
//// * Returns an option that
//// * 
//// * @param validator
//// * @return the option
//// */
////// Hmm integration with vaildation
////static Option validateWiring(BiConsumer<Component, ComponentDriver<?>> validator) {
////    throw new UnsupportedOperationException();
////}
//
//// Option serviceable()
//// Hmm Maaske er alle serviceable.. Og man maa bare lade vaere
//// at expose funktionaliteten.
//}

//And the use one big switch
//Kunne ogsaa encode det i ComponentDriver.option..
//Og saa bruge MethodHandles til at extract id, data?