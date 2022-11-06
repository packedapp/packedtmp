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
public abstract class AbstractComposer {

    ComposerAssembly<?> assembly;

    /**
     * Checks that the underlying container is still configurable.
     */
    protected final void checkIsConfigurable() {
        if (container().handle.container.assembly.isClosed()) {
            throw new IllegalStateException("This composer is no longer configurable");
        }
    }

    /**
     * @return
     * 
     * @see Assembly#container()
     */
    // Maybe another so can be exposed as container?
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
    public static abstract class ComposerAssembly<C extends AbstractComposer> extends ContainerAssembly {
        private final C composer;
        private final ComposerAction<? super C> action;

        // Kunne tage en Supplier<C> og saa bruge en ExtentLocal
        protected ComposerAssembly(C composer, ComposerAction<? super C> action) {
            this.composer = requireNonNull(composer, "composer is null");
            composer.assembly = this;
            this.action = requireNonNull(action, "action is null");
        }

        /** {@inheritDoc} */
        @Override
        protected void build() {
            // reset lookup?
            action.build(composer);
        }
    }
}
