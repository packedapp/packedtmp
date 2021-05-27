package packed.internal.component.bean;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

import app.packed.base.Nullable;
import app.packed.component.BaseBeanConfiguration;
import app.packed.component.BeanConfiguration;
import app.packed.component.BeanDriver;
import app.packed.component.BeanDriver.Binder;
import app.packed.component.BeanKind;
import app.packed.component.ComponentConfiguration;
import app.packed.component.Wirelet;
import app.packed.container.Extension;
import app.packed.inject.Factory;
import app.packed.service.ServiceBeanConfiguration;
import packed.internal.component.ComponentSetup;
import packed.internal.invoke.Infuser;

/** Implementation of {@link BeanDriver.Binder}. */
@SuppressWarnings({ "unchecked", "rawtypes" })
public final class PackedBeanDriverBinder<T, C extends BeanConfiguration> implements BeanDriver.Binder<T, C> {

    /** A {@link BeanKind#SINGLETON} bean binder. */
    public static final PackedBeanDriverBinder<Object, BaseBeanConfiguration> SINGLETON_BEAN_BINDER = PackedBeanDriverBinder.of(MethodHandles.lookup(),
            ServiceBeanConfiguration.class, BeanKind.SINGLETON);

    /** A {@link BeanKind#STATIC} bean binder. */
    public static final PackedBeanDriverBinder<Object, BaseBeanConfiguration> STATIC_BEAN_BINDER = PackedBeanDriverBinder.of(MethodHandles.lookup(),
            BaseBeanConfiguration.class, BeanKind.STATIC);

    final MethodHandle constructor;

    final BeanKind kind;

    @Nullable
    final Wirelet wirelet;

    public PackedBeanDriverBinder(@Nullable Wirelet wirelet,  MethodHandle constructor, BeanKind kind) {
        this.wirelet = wirelet;
        this.kind = requireNonNull(kind);
        this.constructor = constructor;
    }

    /** {@inheritDoc} */
    @Override
    public PackedBeanDriver<C> bind(Class<? extends T> implementation) {
        requireNonNull(implementation, "implementation is bull");
        return new PackedBeanDriver(wirelet, this, implementation, implementation);
    }

    /** {@inheritDoc} */
    @Override
    public PackedBeanDriver<C> bind(Factory<? extends T> factory) {
        requireNonNull(factory, "factory is bull");
        if (kind == BeanKind.STATIC) {
            throw new UnsupportedOperationException("Cannot bind a factory to a static bean.");
        }
        return new PackedBeanDriver(wirelet, this, factory.rawType(), factory);
    }

    /** {@inheritDoc} */
    @Override
    public PackedBeanDriver<C> bindInstance(T instance) {
        requireNonNull(instance, "instance is null");
        if (Class.class.isInstance(instance)) {
            throw new IllegalArgumentException("Cannot bind a Class instance to this method, was " + instance);
        } else if (Factory.class.isInstance(instance)) {
            throw new IllegalArgumentException("Cannot bind a Factory instance to this method, was " + instance);
        } else if (kind != BeanKind.SINGLETON) {
            throw new UnsupportedOperationException("Can only bind instances to singleton beans, kind = " + kind);
        }
        return new PackedBeanDriver(wirelet, this, instance.getClass(), instance);
    }

    @Override
    public Optional<Class<? extends Extension>> extension() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BeanKind kind() {
        return kind;
    }

    @Override
    public Binder<T, C> with(Wirelet... wirelet) {
        throw new UnsupportedOperationException();
    }

    public static <T, C extends BeanConfiguration> PackedBeanDriverBinder<T, C> of(MethodHandles.Lookup caller, Class<? extends C> driverType,
            BeanKind kind) {

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

        return new PackedBeanDriverBinder(null, constructor, kind);
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