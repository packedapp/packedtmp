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
package app.packed.assembly;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles.Lookup;

import app.packed.container.ContainerConfiguration;
import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;
import app.packed.util.Nullable;
import internal.app.packed.application.PackedApplicationInstaller;
import internal.app.packed.assembly.AssemblySetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.PackedContainerInstaller;

/**
 * A composer is
 */

// Operere vi pa Extensions? eller Extenpoints??
// Maybe sealed for now... Until I figure out exact what is allowed
public abstract class AbstractComposer {

    /**
     * The configuration of the container that this composer defines.
     * <p>
     * The value of this field goes through 3 states:
     * <p>
     * <ul>
     * <li>Initially, this field is null, indicating that the composer has not yet been used to build anything.</li>
     * <li>Then, as a part of the build process, it is initialized with a container configuration object.</li>
     * <li>Finally, {@link ContainerConfiguration#USED} is set to indicate that the composer has been used.</li>
     * </ul>
     * <p>
     * This field is updated via var handle {@link #VH_CONFIGURATION}.
     */
    @Nullable
    AssemblyConfiguration configuration;

    /** {@return the base extension.} */
    protected BaseExtension base() {
        return use(BaseExtension.class);
    }

    /**
     * Returns the configuration of the <strong>root</strong> container defined by this composer.
     * <p>
     * This method can only be called from within the {@link ComposerAction#build(AbstractComposer)} method. Otherwise, an
     * {@link IllegalStateException} is thrown.
     *
     * @return the configuration of the container this composer defines
     * @throws IllegalStateException
     *             if called from outside of {@link ComposerAction#build(AbstractComposer)}
     */
    protected ContainerConfiguration configuration() {
        AssemblyConfiguration c = configuration;
        if (c == null) {
            throw new IllegalStateException("This method cannot be called from the constructor of an assembly");
        } else if (c == AssemblyConfiguration.USED) {
            throw new IllegalStateException("Cannot call this method outside of ComposerAction::build(Composer)");
        }
        return c.assembly.container.configuration();
    }

    /**
     * Specifies a lookup object that the framework will use will be used when access bean members installed from within
     * this assembly.
     * <p>
     * This method can be used as an alternative
     * <p>
     * Example
     *
     * <p>
     * The lookup object passed to this method is only to used to access members on beans that are installed via this
     * composer. It is never exposed directly to extensions.
     * <p>
     * This method is typically never called more than once.
     *
     * @param lookup
     *            the lookup object
     * @throws IllegalStateException
     *             if called from outside of {@link #build()}
     * @see BuildableAssembly#lookup(Lookup)
     */
    public final void lookup(Lookup lookup) {
        requireNonNull(lookup, "lookup cannot be null");
        ContainerSetup.crack(configuration()).assembly.lookup(lookup);
    }

    protected void preCompose() {}

    protected <E extends Extension<?>> E use(Class<E> extensionClass) {
        return configuration().use(extensionClass);
    }

    /** A special type of assembly that uses a composer and a composer action to configure an application. */
    public static abstract non-sealed class ComposableAssembly<C extends AbstractComposer> extends Assembly {

        /** The action to run. */
        private final ComposerAction<? super C> action;

        /** The composer argument to the action */
        private final C composer;

        protected ComposableAssembly(C composer, ComposerAction<? super C> action) {
            this.composer = requireNonNull(composer, "composer is null");
            this.action = requireNonNull(action, "action is null");
        }

        /**
         * Invoked by the runtime (via a MethodHandle). This method is mostly machinery that makes sure that the assembly is not
         * used more than once.
         *
         * @param builder
         *            the configuration to use for the assembling process
         */
        @Override
        AssemblySetup build(@Nullable PackedApplicationInstaller<?> applicationInstaller, PackedContainerInstaller<?> installer) {
//            if (builder instanceof @SuppressWarnings("unused") PackedContainerInstaller installer) {
//                throw new IllegalArgumentException("Cannot link an instance of " + ComposableAssembly.class + ", assembly must extend "
//                        + BuildableAssembly.class.getSimpleName() + " instead");
//            }

            AssemblyConfiguration existing = composer.configuration;
            if (existing == null) {
                AssemblySetup a = AssemblySetup.newAssembly(installer, this);
                AssemblyConfiguration as = composer.configuration = existing = new AssemblyConfiguration(a);
                try {
                    composer.preCompose();

                    a.model.hooks.forEach(AssemblyBuildHook.class, h -> h.beforeBuild(as));
                    // Run AssemblyHook.onPreBuild if hooks are present

                    // Call actions build method with this composer
                    action.build(composer);

                    // Run AssemblyHook.onPostBuild if hooks are present
                    a.model.postBuild(existing);
                } finally {
                    // Sets #configuration to a marker object that indicates the assembly has been used
                    composer.configuration = AssemblyConfiguration.USED;
                }
                a.postBuild();
                return a;
            } else if (existing == AssemblyConfiguration.USED) {
                // Assembly has already been used (successfully or unsuccessfully)
                throw new IllegalStateException("This assembly has already been used, assembly = " + getClass());
            } else {
                // Assembly is in the process of being used. Typically happens, if an assembly is linked recursively.
                throw new IllegalStateException("This assembly is currently being used elsewhere, assembly = " + getClass());
            }
        }
    }

    /** Represents an operation that operates on a composer. */
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
}
