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
import java.lang.invoke.VarHandle;

import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;
import app.packed.framework.Nullable;
import internal.app.packed.container.AssemblyModel;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.util.LookupUtil;

/**
 *
 */
public abstract class AbstractComposer {

    /** A var handle that can update the {@link #configuration} field in this class. */
    private static final VarHandle VH_CONFIGURATION = LookupUtil.findVarHandleOwn(MethodHandles.lookup(), "configuration", ContainerConfiguration.class);

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
    ContainerConfiguration configuration;

    /** {@return the base extension.} */
    protected BaseExtension base() {
        return use(BaseExtension.class);
    }

    protected void preCompose() {}

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
        ContainerConfiguration c = configuration;
        if (c == null) {
            throw new IllegalStateException("This method cannot be called from the constructor of an assembly");
        } else if (c == ContainerConfiguration.USED) {
            throw new IllegalStateException("Cannot call this method outside of ComposerAction::build(Composer)");
        }
        return c;
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
     */
    public final void lookup(Lookup lookup) {
        requireNonNull(lookup, "lookup cannot be null");
        configuration().handle.container.assembly.lookup(lookup);
    }

    protected <E extends Extension<?>> E use(Class<E> extensionClass) {
        return configuration().use(extensionClass);
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

    /**
     * A special assembly that accompanies a composer.
     */
    public static abstract non-sealed class ComposerAssembly<C extends AbstractComposer> extends Assembly {

        /** The action to run. */
        private final ComposerAction<? super C> action;

        /** The composer argument to the action */
        private final C composer;

        protected ComposerAssembly(C composer, ComposerAction<? super C> action) {
            this.composer = requireNonNull(composer, "composer is null");
            this.action = requireNonNull(action, "action is null");
        }

        /**
         * Invoked by the runtime (via a MethodHandle). This method is mostly machinery that makes sure that the assembly is not
         * used more than once.
         *
         * @param assembly
         *            the realm used to call container hooks
         * @param configuration
         *            the configuration to use for the assembling process
         */
        void doBuild(AssemblyModel assemblyModel, ContainerSetup container) {
            ContainerConfiguration configuration = new ContainerConfiguration(new ContainerHandle(container));
            // Do we really need to guard against concurrent usage of an assembly?
            Object existing = VH_CONFIGURATION.compareAndExchange(composer, null, configuration);
            if (existing == null) {
                try {
                    composer.preCompose();

                    // Run AssemblyHook.onPreBuild if hooks are present
                    assemblyModel.preBuild(configuration);

                    // Call actions build method with this composer
                    action.build(composer);

                    // Run AssemblyHook.onPostBuild if hooks are present
                    assemblyModel.postBuild(configuration);
                } finally {
                    // Sets #configuration to a marker object that indicates the assembly has been used
                    VH_CONFIGURATION.setVolatile(composer, ContainerConfiguration.USED);
                }
            } else if (existing == ContainerConfiguration.USED) {
                // Assembly has already been used (successfully or unsuccessfully)
                throw new IllegalStateException("This assembly has already been used, assembly = " + getClass());
            } else {
                // Assembly is in the process of being used. Typically happens, if an assembly is linked recursively.
                throw new IllegalStateException("This assembly is currently being used elsewhere, assembly = " + getClass());
            }
        }
    }
}
