package packed.internal.component;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Set;

import app.packed.component.BaseComponentConfiguration;
import app.packed.component.ClassComponentDriver;
import app.packed.component.ClassComponentMode;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.Wirelet;
import app.packed.inject.Factory;
import app.packed.inject.ServiceComponentConfiguration;
import packed.internal.component.PackedClassComponentDriver.Type;
import packed.internal.component.PackedComponentDriver.BoundClassComponentDriver;
import packed.internal.invoke.Infuser;

@SuppressWarnings({ "unchecked", "rawtypes" })
public record PackedClassComponentDriver<T, C extends ComponentConfiguration> (Type type, MethodHandle constructor, int modifiers)
        implements ClassComponentDriver<T, ComponentConfiguration> {

    public static final PackedClassComponentDriver INSTALL_DRIVER = PackedClassComponentDriver.ofInstance(MethodHandles.lookup(), ServiceComponentConfiguration.class, true);

    /** A driver for this configuration. */
    public static final PackedClassComponentDriver STATELESS_DRIVER = PackedClassComponentDriver.ofClass(MethodHandles.lookup(), BaseComponentConfiguration.class);

    
    public enum Type {
        CLASS, FACTORY, INSTANCE;
    }

    @Override
    public BoundClassComponentDriver<ComponentConfiguration> bind(Class<? extends T> implementation) {
        return new BoundClassComponentDriver(this, implementation);
    }

    @Override
    public BoundClassComponentDriver<ComponentConfiguration> bind(Factory<? extends T> factory) {
        return new BoundClassComponentDriver(this, factory);
    }

    @Override
    public BoundClassComponentDriver<ComponentConfiguration> bindInstance(T instance) {
        return new BoundClassComponentDriver(this, instance);
    }

    @Override
    public Set<? extends ClassComponentMode> supportedModes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassComponentDriver<T, ComponentConfiguration> with(Wirelet... wirelet) {
        throw new UnsupportedOperationException();
    }

    private static <T, C extends ComponentConfiguration> PackedClassComponentDriver<T, C> newMeta(Type type, MethodHandles.Lookup caller,
            Class<? extends C> driverType, boolean isConstant) {

        // Parse all options
        int modifiers = 0;
        if (isConstant) {
            modifiers |= PackedComponentModifierSet.I_SINGLETON;
        } else {
            modifiers |= PackedComponentModifierSet.I_STATEFUL;
        }

        modifiers |= PackedComponentModifierSet.I_SOURCE;
        // IDK should we just have a Function<ComponentComposer, T>???
        // Unless we have multiple composer/context objects (which it looks like we wont have)
        // Or we fx support @AttributeProvide... This makes no sense..
        // AttributeProvide could make sense... And then some way to say retain this info at runtime...
        // But maybe this is sidecars instead???

        // Create an infuser for making a method handle for the component configurations's constructor
        Infuser.Builder builder = Infuser.builder(caller, driverType, ComponentSetup.class);
        builder.provide(ComponentConfigurationContext.class).adaptArgument(0);
        MethodHandle constructor = builder.findConstructor(ComponentConfiguration.class, e -> new IllegalArgumentException(e));

        return new PackedClassComponentDriver(type, constructor, modifiers);
    }

    public static <T, C extends ComponentConfiguration> PackedClassComponentDriver<T, C> ofFactory(MethodHandles.Lookup caller, Class<? extends C> driverType,
            boolean isConstant) {

        PackedClassComponentDriver meta = newMeta(Type.FACTORY, caller, driverType, isConstant);
        return meta;
    }

    public static <T, C extends ComponentConfiguration> PackedClassComponentDriver<T, C> ofInstance(MethodHandles.Lookup caller, Class<? extends C> driverType,
            boolean isConstant) {

        PackedClassComponentDriver meta = newMeta(Type.INSTANCE, caller, driverType, isConstant);
        return meta;
    }

    
    public static <T, C extends ComponentConfiguration> PackedClassComponentDriver<T, C> ofClass(MethodHandles.Lookup caller,
            Class<? extends C> configurationType) {
        return newMeta(Type.CLASS, caller, configurationType, false);
    }
}
