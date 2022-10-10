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
package internal.app.packed.bean;

import static java.util.Objects.requireNonNull;

import app.packed.base.Nullable;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanSourceKind;
import internal.app.packed.bean.BeanProps.InstallerOption.CustomIntrospector;
import internal.app.packed.bean.BeanProps.InstallerOption.CustomPrefix;
import internal.app.packed.bean.BeanProps.InstallerOption.NonUnique;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.RealmSetup;

/** Implementation of BeanHandle.Builder. */
public record BeanProps(

        /** The kind of bean. */
        BeanKind kind,

        /** The bean class, is typical void.class for functional beans. */
        Class<?> beanClass,

        /** The type of source the installer is created from. */
        BeanSourceKind sourceKind,

        /** The source ({@code null}, {@link Class}, {@link PackedOp}, or an instance) */
        @Nullable Object source,

        /** A model of hooks on the bean class. Or null if no member scanning was performed. */
        @Nullable BeanClassModel beanModel,

        /** The operator of the bean. */
        ExtensionSetup operator,

        RealmSetup realm,

        @Nullable ExtensionSetup extensionOwner,

        /** A custom bean introspector that may be set via {@link #introspectWith(BeanIntrospector)}. */
        @Nullable BeanIntrospector customIntrospector,

        @Nullable String namePrefix,

        boolean nonUnique) {

    // Eclipse requires permits here.. Compiler bug
    public sealed interface InstallerOption extends BeanHandle.Option permits NonUnique, CustomIntrospector, CustomPrefix {

        static final InstallerOption NON_UNIQUE = new NonUnique();

        default void validate(BeanKind kind) {}

        public record NonUnique() implements InstallerOption {

            /** {@inheritDoc} */
            @Override
            public void validate(BeanKind kind) {
                if (!kind.hasInstances()) {
                    throw new IllegalArgumentException("NonUnique cannot be used with functional beans");
                }
            }
        }

        public record CustomIntrospector(BeanIntrospector introspector) implements InstallerOption {

            public CustomIntrospector {
                requireNonNull(introspector, "introspector is null");
            }

            /** {@inheritDoc} */
            @Override
            public void validate(BeanKind kind) {
                if (!kind.hasInstances()) {
                    throw new IllegalArgumentException("NonUnique cannot be used with functional beans");
                }
            }
        }

        public record CustomPrefix(String prefix) implements InstallerOption {

        }
    }
}
//
///**
// * An installer used to create {@link BeanHandle}. Is created using the various {@code beanInstaller} methods on
// * {@link BeanExtensionPoint}.
// * <p>
// * The main purpose of this interface is to allow various configuration that is needed before the bean is introspected.
// * If the configuration is not needed before introspection the functionality such be present on {@code BeanHandle}
// * instead.
// * 
// * @see BeanExtensionPoint#newFunctionalBean()
// * @see BeanExtensionPoint#beanInstallerFromClass(Class)
// * @see BeanExtensionPoint#newHandleFromOp(Op)
// * @see BeanExtensionPoint#beanBuilderFromInstance(Object)
// */
//// Could have, introspectionDisable()/noIntrospection
//
//@Deprecated
//sealed interface Installer<T> permits PackedBeanHandleInstaller {
//
////    /**
////     * Marks the bean as owned by the extension representing by specified extension point context
////     * 
////     * @param context
////     *            an extension point context representing the extension that owns the bean
////     * @return this builder
////     * @throws IllegalStateException
////     *             if build has previously been called on the builder
////     */
////    Installer<T> forExtension(UseSite context);
//
//    /**
//     * Adds a new bean to the container and returns a handle for it.
//     * 
//     * @return the new handle
//     * @throws IllegalStateException
//     *             if install has previously been called
//     */
//    BeanHandle<T> install();
//
////    /**
////     * There will never be any bean instances.
////     * <p>
////     * This method can only be used together with {@link BeanExtensionPoint#beanInstallerFromClass(Class)}.
////     * 
////     * @return this installer
////     * @throws IllegalStateException
////     *             if used without source kind {@code class}
////     */
////    // I think we have an boolean instantiate on beanInstallerFromClass
////    Installer<T> instanceless();
////
////    /**
////     * Registers a bean introspector that will be used instead of the framework calling
////     * {@link Extension#newBeanIntrospector}.
////     * 
////     * @param introspector
////     * @return this builder
////     * 
////     * @throws UnsupportedOperationException
////     *             if the bean has a void bean class
////     * 
////     * @see Extension#newBeanIntrospector
////     */
////    Installer<T> introspectWith(BeanIntrospector introspector);
//
//    // Option.Singleton, Option.lifetimeLazy;
//
//    // Instance -> Altid eager
//
//    // Eager - Singleton
//    // Eager - NonSingleton
//    // Lazy - Singleton
//    // Lazy - NonSingleton
//    // Many
////    Installer<T> kindSingleton();
////
////    Installer<T> kindUnmanaged();
//
//}