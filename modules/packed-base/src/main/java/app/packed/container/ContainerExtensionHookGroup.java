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
package app.packed.container;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import app.packed.app.Main;
import app.packed.component.ComponentConfiguration;
import app.packed.hook.AnnotatedFieldHook;
import app.packed.lifecycle.LifecycleExtension;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.MethodDescriptor;
import packed.internal.componentcache.ExtensionHookGroupConfiguration;
import packed.internal.container.PackedContainer;
import packed.internal.support.AppPackedLifecycleSupport;
import packed.internal.util.StringFormatter;

/**
 *
 */
// checkAnnotatedFieldReadable();
// checkAnnotatedFieldWriteable();

// Checks

// read field
// write field
// read-write field
// InvokeMethod

// Kan vi lave en cachable version, og en ikke cacheable version???? 2 forskellige klasser...

// Vi registrere kun callbacks
// Alternativ kan vi registere visitor

// registerAnnotatedMethod(Provide.class, new ProvideVisitior(){});

public abstract class ContainerExtensionHookGroup<E extends ContainerExtension<E>, B extends Supplier<BiConsumer<ComponentConfiguration, E>>> {

    ExtensionHookGroupConfiguration.Builder builder;

    /**
     * Returns a hook group configuration builder or fails with {@link IllegalStateException} if not called from within
     * {@link #configure()}.
     * 
     * @return a hook group configuration builder
     */
    ExtensionHookGroupConfiguration.Builder builder() {
        ExtensionHookGroupConfiguration.Builder b = builder;
        if (b == null) {
            throw new IllegalStateException(
                    "This method can only be invoked from within " + ContainerExtensionHookGroup.class.getSimpleName() + ".configure()");
        }
        return builder;
    }

    /**
     * This method is normally only invoked once for a given subclass and then cached.
     */
    protected abstract void configure();

    /**
     * Creates a new builder.
     * 
     * @param componentType
     *            the component type
     * @return the new builder
     */
    // Do we need an instantiation mode as an additional parameter??
    public abstract B newBuilder(Class<?> componentType);

    protected final <A extends Annotation> void onAnnotatedField(Class<A> annotationType, BiConsumer<B, AnnotatedFieldHook<A>> consumer) {

    }

    protected final <A extends Annotation> void onAnnotatedMethod(Class<A> annotationType, BiConsumer<B, AnnotatedMethodHook<A>> consumer) {}

    protected final <A extends Annotation> void onAnnotatedMethodDescription(Class<A> annotationType, BiConsumer<B, MethodDescriptor> consumer) {}

    protected final <A extends Annotation> void onAnnotatedMethodHandle(Class<A> annotationType, BiConsumer<B, MethodHandle> consumer) {}

    protected final <A extends Annotation> void onTypeAnnotation(Class<A> annotationType, BiConsumer<B, A> consumer) {
        throw new UnsupportedOperationException();
    }

    // Eller kan extensionen selv klare det??? Ja for nu
    final void requireExtension(Class<? extends ContainerExtension<?>> extensionType) {}

    // // allowPrivateFields()
    // public interface Builder<E extends Extension<E>> {
    //
    // /**
    // * Returns a consumer that will be invoked every time a component of the particular type is installed.
    // *
    // * @return a consumer that will be invoked every time a component of the particular type is installed
    // * @throws RuntimeException
    // * if the some property of the component type was invalid
    // */
    // BiConsumer<ComponentConfiguration, E> build();
    // }
}

/** Takes care of component methods annotated with {@link Main}. */
final class MainExtensionHookGroup extends ContainerExtensionHookGroup<LifecycleExtension, MainExtensionHookGroup.Builder> {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        onAnnotatedMethod(Main.class, (b, m) -> b.add(m));
    }

    /** {@inheritDoc} */
    @Override
    public Builder newBuilder(Class<?> componentType) {
        return new Builder();
    }

    static class Builder implements Supplier<BiConsumer<ComponentConfiguration, LifecycleExtension>> {

        private AnnotatedMethodHook<Main> hook;

        private void add(AnnotatedMethodHook<Main> hook) {
            if (this.hook != null) {
                throw new InvalidDeclarationException("A component of the type '" + StringFormatter.format(hook.method().getDeclaringClass())
                        + "' defined more than one method annotated with @" + Main.class.getSimpleName() + ", Methods = "
                        + StringFormatter.formatShortWithParameters(this.hook.method()) + ", " + StringFormatter.formatShortWithParameters(hook.method()));
            }
            this.hook = hook;
        }

        /** {@inheritDoc} */
        @Override
        public BiConsumer<ComponentConfiguration, LifecycleExtension> get() {
            MethodHandle mh = hook.newMethodHandle();
            hook.onMethodReady(PackedContainer.class, (a, b) -> {
                b.run();
            });

            // Vi skal bruge denne her fordi, vi bliver noedt til at checke at vi ikke har 2 komponenter med @main
            return (c, e) -> AppPackedLifecycleSupport.invoke().doConfigure(e, mh);
        }
    }
}
//// Er det maaden vi ogsaa skal fikse hooks paa...
// public abstract class ExtensionMethodConfigurator {
//
// protected abstract void configure();
//
// protected final void requireExtension(Class<? extends Extension<?>> extensionType) {}
//
// protected final void enabledInjection() {}
//
// protected final void addInjectable(Key<?> key, String description) {}
//
// protected final void addInjectable(Class<?> key, String description) {}
// }
//
//// MethodHandles must be installed via a SupportBundle...
//
//// Maaske skal den installeres som en service i modulet, der definere annoteringen....
// class ProvidesConfiguator extends ExtensionMethodConfigurator {
//
// /** {@inheritDoc} */
// @Override
// protected void configure() {
// requireExtension(InjectorExtension.class);
// enabledInjection();
// addInjectable(ProvideHelper.class, "A helper object for @Provides");
// }
// }
//// DisableStaticFields();
//// DisableStaticMethods();
//// Type....
//// Vi har installeret denne component, med denne annotering....
//
//// extends ExtensionMethodConfigurator<Provides, InjectionExtension>
//// void onTypeCallback(BiConsumer<InjectionExtension, Provides, Class>) //AnnotatedTypeHook?
//// void onFieldCallback(BiConsumer<InjectionExtension, Provides, ReadWritableField>) //AnnotatedFieldHook?
//
///// Regler.... Har vi instans ogsaa???
//// Det er jo noejagtig det samme som
//
/////// Giver det mening at suppurtere kald paa en maade for extensions
/////// Og en anden maade for hooks
//// Declarativt
//
////// Extension hooks
//
////// Hooks -> General available for anyone, part of an API
////// Extension Hooks -> Managed by an Extension, is never exported, cannot be captured by @OnHook??? Or????
//
////// Vi har noget instance, vi har noget static
/////// F.eks. onType....
