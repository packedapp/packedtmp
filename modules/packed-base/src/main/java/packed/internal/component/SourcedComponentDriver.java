/*
 * Copyright (c) 2008 Kasper Nielsen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.base.Nullable;
import app.packed.component.BaseComponentConfiguration;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.ComponentDriver;
import app.packed.component.ComponentModifierSet;
import app.packed.component.Wirelet;
import app.packed.inject.Factory;
import app.packed.inject.ServiceComponentConfiguration;
import packed.internal.application.ApplicationSetup;
import packed.internal.invoke.Infuser;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
public class SourcedComponentDriver<C extends ComponentConfiguration> extends WireableComponentDriver<C> {

    @SuppressWarnings("rawtypes")
    public static final ComponentDriver INSTALL_DRIVER = SourcedComponentDriver.ofInstance(MethodHandles.lookup(), ServiceComponentConfiguration.class, true);

    /** A driver for this configuration. */
    @SuppressWarnings("rawtypes")
    public static final ComponentDriver STATELESS_DRIVER = SourcedComponentDriver.ofClass(MethodHandles.lookup(), BaseComponentConfiguration.class);

    @Nullable
    public final Object binding;

    final Inner inner;

    SourcedComponentDriver(Inner meta, Object data) {
        super(null, PackedComponentModifierSet.intOf(meta.modifiersSet().toArray()));
        this.inner = requireNonNull(meta);
        this.binding = data;
        if (modifiers == 0) {
            throw new IllegalStateException();
        }
    }

    @Override
    public ComponentDriver<C> bind(Object object) {
        requireNonNull(object, "object is null");
        if (binding != null) {
            throw new IllegalStateException("This driver has already been bound");
        }
        if (inner.type == Type.FACTORY) {
            if (Class.class.isInstance(object)) {
                // throw new IllegalArgumentException("Cannot bind a Class instance, was " + object);
            }
        } else if (inner.type == Type.INSTANCE) {
            if (Class.class.isInstance(object)) {
                // throw new IllegalArgumentException("Cannot bind a Class instance, was " + object);
            } else if (Factory.class.isInstance(object)) {
                // throw new IllegalArgumentException("Cannot bind a Factory instance, was " + object);
            }
        }
        return new SourcedComponentDriver<>(inner, object);
    }

    public void checkBound() {
        inner.checkBound(this);
    }

    /** {@inheritDoc} */
    @Override
    public ComponentModifierSet modifiers() {
        return inner.modifiersSet;
    }

    public SourcedComponentSetup newComponent(ApplicationSetup application, RealmSetup realm, @Nullable ComponentSetup parent, Wirelet[] wirelets) {
        requireNonNull(parent);
        return new SourcedComponentSetup(application,  realm, this, parent, wirelets);
    }

    public C toConfiguration(ComponentConfigurationContext cnc) {
        // Vil godt lave den om til CNC
        try {
            return (C) inner.mh.invoke(cnc);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    @Override
    protected ComponentDriver<C> withWirelet(Wirelet w) {
        throw new UnsupportedOperationException();
    }

    private static Inner newMeta(Type type, MethodHandles.Lookup caller, Class<?> driverType, boolean isConstant) {

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

        return new Inner(type, constructor, modifiers);
    }

    public static <C extends ComponentConfiguration> ComponentDriver<C> ofClass(MethodHandles.Lookup caller, Class<? extends C> driverType) {
        return new SourcedComponentDriver<>(newMeta(Type.CLASS, caller, driverType, false), null);
    }

    public static <C extends ComponentConfiguration> ComponentDriver<C> ofFactory(MethodHandles.Lookup caller, Class<? extends C> driverType,
            boolean isConstant) {

        Inner meta = newMeta(Type.FACTORY, caller, driverType, isConstant);
        return new SourcedComponentDriver<>(meta, null);
    }

    public static <C extends ComponentConfiguration> ComponentDriver<C> ofInstance(MethodHandles.Lookup caller, Class<? extends C> driverType,
            boolean isConstant) {

        Inner meta = newMeta(Type.INSTANCE, caller, driverType, isConstant);
        return new SourcedComponentDriver<>(meta, null);
    }

    record Inner(Type type, MethodHandle mh, int modifiers, PackedComponentModifierSet modifiersSet) {

        Inner(Type type, MethodHandle mh, int modifiers) {
            this(type, mh, modifiers, new PackedComponentModifierSet(modifiers));
        }

        void checkBound(SourcedComponentDriver<?> driver) {

        }
    }

    enum Type {
        CLASS, FACTORY, INSTANCE;
    }
}

//public interface Option {
//
//  /**
//   * The component the driver will be a container.
//   * <p>
//   * A container that is a component cannot be sourced??? Yes It can... It can be the actor system
//   * 
//   * @return stuff
//   * @see ComponentModifier#CONSTANT
//   */
//  // InstanceComponentDriver automatically sets the source...
////  static Option sourceAssignableTo(Class<?> rawType) {
////      throw new UnsupportedOperationException();
////  }
//
////
////  static Option validateParent(Predicate<? super Component> validator, String msg) {
////      return validateWiring((c, d) -> {
////          if (validator.test(c)) {
////              throw new IllegalArgumentException(msg);
////          }
////      });
////  }
////
////  static Option validateParentIsContainer() {
////      return validateParent(c -> c.hasModifier(ComponentModifier.CONTAINER), "This component can only be wired to a container");
////  }
//
//  // The parent + the driver
//  //
////
////  /**
////   * Returns an option that
////   * 
////   * @param validator
////   * @return the option
////   */
////  // Hmm integration with vaildation
////  static Option validateWiring(BiConsumer<Component, ComponentDriver<?>> validator) {
////      throw new UnsupportedOperationException();
////  }
//
//  // Option serviceable()
//  // Hmm Maaske er alle serviceable.. Og man maa bare lade vaere
//  // at expose funktionaliteten.
//}

// And the use one big switch
// Kunne ogsaa encode det i ComponentDriver.option..
// Og saa bruge MethodHandles til at extract id, data?
// Nahhh