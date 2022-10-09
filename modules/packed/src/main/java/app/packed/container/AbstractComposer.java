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

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

/**
 * Composers does not usually have any public constructors.
 * 
 * Unlike applications created with an assembly.
 * 
 * You cannot build images/launchers You cannot get a mirror
 * 
 * A composer will always instantiate a single application instance
 */
// (synthetic Assembly -> Generaeted per module-per composer type???) Men hvis man ikke kan lave mirrors???
// Altsaa man kan vel altid faa dem injected. Med mindre vi disable MirrorExtension...
public abstract class AbstractComposer {

    ComposerAssembly<?> assembly;

    /**
     * Checks that the underlying container is still configurable.
     */
    protected final void checkIsConfigurable() {
        if (container().container.assembly.isClosed()) {
            throw new IllegalStateException("This composer is no longer configurable");
        }
    }

    /**
     * @return
     * 
     * @see Assembly#container()
     */
    protected final ContainerConfiguration container() {
        ContainerConfiguration c = assembly.configuration;
        if (c == null) {
            throw new IllegalStateException("This method cannot be called from the constructor of a composer");
        } else if (c == ContainerConfiguration.USED) {
            throw new IllegalStateException("This method must be called from with ComposerAction::build");
        }
        return c;
    }

    /**
     * Sets a {@link Lookup lookup object} that will be used to access members (fields, constructors and methods) on
     * registered objects. The lookup object will be used for all service bindings and component installations that happens
     * after the invocation of this method.
     * <p>
     * This method can be invoked multiple times. In all cases the object being bound or installed will use the latest
     * registered lookup object.
     * <p>
     * If no lookup is specified using this method, the runtime will use the public lookup object
     * ({@link MethodHandles#publicLookup()}) for member access.
     *
     * @param lookup
     *            the lookup object
     */
    public final void lookup(MethodHandles.Lookup lookup) {
        assembly.lookup(lookup);
    }

//    /**
//     * Invoked by the runtime immediately after {@link ComposerAction#build(AbstractComposer)}.
//     * <p>
//     * This method will not be called if {@link ComposerAction#build(AbstractComposer)} throws an exception.
//     */
//    protected void onConfigured() {} // onComposed or onBuilt, onPreConfigure/ onPostConfigur
//
//    /**
//     * Invoked by the runtime immediately before it invokes {@link ComposerAction#build(AbstractComposer)}. Used for any
//     * configuration that needs to be done before control is handed over to the composer action specified by the user.
//     */
//    protected void onNew() {} // navngivningen skal alines med AssemblyHook
//
//    /**
//     * Create a new application instance by using the specified consumer and configurator.
//     * <p>
//     * This method is is never called directly by end-users. But indirectly through methods such as
//     * {@link ServiceLocator#of(ComposerAction, Wirelet...)}.
//     * 
//     * @param <A>
//     *            the application type
//     * @param <C>
//     *            the type of composer that is exposed to the end-user
//     * @param driver
//     *            the application driver
//     * @param assemblyClass
//     *            an assembly class
//     * @param composer
//     *            the composer
//     * @param action
//     *            the build action that operates on a composer
//     * @param wirelets
//     *            optional wirelets
//     * @return the launch result
//     */
//    protected static <A, C extends AbstractComposer> A compose(ApplicationDriver<A> driver, Class<? extends ComposerAssembly> assemblyClass, C composer,
//            ComposerAction<? super C> action, Wirelet... wirelets) {
//        PackedApplicationDriver<A> d = (PackedApplicationDriver<A>) requireNonNull(driver, "driver is null");
//        requireNonNull(assemblyClass, "assemblyClass is null");
//        // Maybe it needs to be a proper subclass?
//        // And maybe it must be in the same module as the composer itself
//        if (!ComposerAssembly.class.isAssignableFrom(assemblyClass)) {
//            throw new ClassCastException(assemblyClass + " must be assignable to " + ComposerAssembly.class);
//        }
//        // Create a new realm
//        ComposerAssemblySetup realm = new ComposerAssemblySetup(d, assemblyClass, composer, action, wirelets);
//
//        // Build the application
//        realm.build();
//
//        // Return a launched application
//        return ApplicationInitializationContext.launch(d, realm.application, /* no runtime wirelets */ null);
//    }

    /**
    *
    */
    @FunctionalInterface
    public interface ComposerAction<C extends AbstractComposer> {

        /**
         * Builds an application using the given composer.
         *
         * @param composer
         *            the composer
         */
        void build(C composer);
    }

    // Kan annoteres og man kan override build
    // Assembly and composer must be in the same module
    public static abstract class ComposerAssembly<C extends AbstractComposer> extends Assembly {
        private final C composer;
        private final ComposerAction<? super C> action;

        protected ComposerAssembly(C composer, ComposerAction<? super C> action) {
            this.composer = requireNonNull(composer, "composer is null");
            this.action = requireNonNull(action, "action is null");
            composer.assembly = this;
        }

        /** {@inheritDoc} */
        @Override
        protected void build() {
            // reset lookup?
            action.build(composer);
        }
    }
}
