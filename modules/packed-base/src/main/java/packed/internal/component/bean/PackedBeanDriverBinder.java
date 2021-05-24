package packed.internal.component.bean;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.Set;

import app.packed.component.BaseBeanConfiguration;
import app.packed.component.BeanConfiguration;
import app.packed.component.BeanDriver;
import app.packed.component.BeanDriver.Binder;
import app.packed.component.BeanMirror.BeanMode;
import app.packed.component.ComponentConfiguration;
import app.packed.component.Wirelet;
import app.packed.container.Extension;
import app.packed.inject.Factory;
import app.packed.inject.ServiceBeanConfiguration;
import packed.internal.component.ComponentSetup;
import packed.internal.invoke.Infuser;

@SuppressWarnings({ "unchecked", "rawtypes" })
public record PackedBeanDriverBinder<T, C extends BeanConfiguration> (PackedBeanDriverBinder.Type type, MethodHandle constructor, int modifiers,
        boolean isConstant) implements BeanDriver.Binder<T, C> {

    public static final PackedBeanDriverBinder<Object, ServiceBeanConfiguration> APPLET_BINDER = PackedBeanDriverBinder.ofInstance(MethodHandles.lookup(),
            ServiceBeanConfiguration.class, true);

    /** A driver for this configuration. */
    public static final PackedBeanDriverBinder<Object, BaseBeanConfiguration> STATELESS_BINDER = PackedBeanDriverBinder.ofClass(MethodHandles.lookup(),
            BaseBeanConfiguration.class);

    public enum Type {
        CLASS, FACTORY, INSTANCE;
    }

    /** {@inheritDoc} */
    @Override
    public PackedBeanDriver<C> bind(Class<? extends T> implementation) {
        requireNonNull(implementation, "implementation is bull");
        return new PackedBeanDriver(this, implementation);
    }

    /** {@inheritDoc} */
    @Override
    public PackedBeanDriver<C> bind(Factory<? extends T> factory) {
        requireNonNull(factory, "factory is bull");
//      if (inner.type == Type.FACTORY) {
//      if (Class.class.isInstance(object)) {
//          // throw new IllegalArgumentException("Cannot bind a Class instance, was " + object);
//      }
        return new PackedBeanDriver(this, factory);
    }

    /** {@inheritDoc} */
    @Override
    public PackedBeanDriver<C> bindInstance(T instance) {
        requireNonNull(instance, "instance is null");
        if (Class.class.isInstance(instance)) {
            throw new IllegalArgumentException("Cannot bind a Class instance, was " + instance);
        } else if (Factory.class.isInstance(instance)) {
            throw new IllegalArgumentException("Cannot bind a Factory instance, was " + instance);
        }
        return new PackedBeanDriver(this, instance);
    }

    @Override
    public Set<? extends BeanMode> supportedModes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Binder<T, C> with(Wirelet... wirelet) {
        throw new UnsupportedOperationException();
    }

    private static <T, C extends BeanConfiguration> PackedBeanDriverBinder<T, C> newMeta(Type type, MethodHandles.Lookup caller,
            Class<? extends C> driverType, boolean isConstant) {

        // IDK should we just have a Function<ComponentComposer, T>???
        // Unless we have multiple composer/context objects (which it looks like we wont have)
        // Or we fx support @AttributeProvide... This makes no sense..
        // AttributeProvide could make sense... And then some way to say retain this info at runtime...
        // But maybe this is sidecars instead???

        // Create an infuser for making a method handle for the component configurations's constructor
        Infuser.Builder builder = Infuser.builder(caller, driverType, ComponentSetup.class);

        // TODO Tror godt vi vil injecte baade wirelets
        // SelectWirelets<?>?? og extension

        MethodHandle constructor = builder.findConstructor(ComponentConfiguration.class, e -> new IllegalArgumentException(e));

        return new PackedBeanDriverBinder(type, constructor, 0, isConstant);
    }

    public static <T, C extends BeanConfiguration> PackedBeanDriverBinder<T, C> ofFactory(MethodHandles.Lookup caller, Class<? extends C> driverType,
            boolean isConstant) {

        PackedBeanDriverBinder meta = newMeta(Type.FACTORY, caller, driverType, isConstant);
        return meta;
    }

    public static <T, C extends BeanConfiguration> PackedBeanDriverBinder<T, C> ofInstance(MethodHandles.Lookup caller, Class<? extends C> driverType,
            boolean isConstant) {

        PackedBeanDriverBinder meta = newMeta(Type.INSTANCE, caller, driverType, isConstant);
        return meta;
    }

    public static <T, C extends BeanConfiguration> PackedBeanDriverBinder<T, C> ofClass(MethodHandles.Lookup caller,
            Class<? extends C> configurationType) {
        return newMeta(Type.CLASS, caller, configurationType, false);
    }

    @Override
    public Optional<Class<? extends Extension>> extension() {
        // TODO Auto-generated method stub
        return null;
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