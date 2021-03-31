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

import java.util.function.Consumer;

import app.packed.base.Nullable;
import app.packed.component.Component;
import app.packed.component.ComponentScope;
import app.packed.component.Wirelet;
import packed.internal.application.ApplicationLaunchContext;
import packed.internal.application.ApplicationSetup;

/** A type of wirelet where its logic is directly embedded in the wirelet. */
public abstract class InternalWirelet extends Wirelet {

    /**
     * Checks that the specified component is the root component of an application.
     * 
     * @param component
     *            the component that is being wired
     * @throws IllegalArgumentException
     *             if the specified component is not the root component of an application
     * @return the application of the component (for method chaining)
     */
    protected ApplicationSetup checkIsApplication(ComponentSetup component) {
        ApplicationSetup application = component.application;
        ComponentSetup parent = component.parent;
        if (parent != null && application == parent.application) {
            throw new IllegalArgumentException("This wirelet can only be specified when wiring an application, wirelet = " + this);
        }
        return component.application;
    }

    protected abstract void onBuild(ComponentSetup component);

    public void onImageInstantiation(ComponentSetup component, ApplicationLaunchContext context) {
        throw new IllegalArgumentException("The wirelet {" + getClass().getSimpleName() + "} cannot be specified when instantiating an image");
    }

    /** {@inheritDoc} */
    public String toString() {
        return getClass().getSimpleName();
    }

    /** A wirelet that will perform a given action once the component has been fully wired. */
    public static final class OnWireActionWirelet extends InternalWirelet {

        /** The action to perform on the component after it has been fully wired. */
        private final Consumer<? super Component> action;

        /** The scope of the wirelet */
        @Nullable
        final ComponentScope scope;

        public OnWireActionWirelet(Consumer<? super Component> action, ComponentScope scope) {
            this.action = requireNonNull(action, "action is null");
            this.scope = scope;
        }

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected void onBuild(ComponentSetup component) {
            // Hmm. Vi vil nok snare have en liste nu, hvis vi har mere end 2
            Consumer<? super Component> existing = component.onWire;
            if (existing == null) {
                component.onWire = action;
            } else {
                component.onWire = existing.andThen((Consumer) action);
            }
        }
    }

    /** A wirelet that will set the name of the component. Used by {@link Wirelet#named(String)}. */
    public static final class OverrideNameWirelet extends InternalWirelet {

        /** The (validated) name to override with. */
        final String name;

        /**
         * Creates a new name wirelet
         * 
         * @param name
         *            the name to override any existing container name with
         */
        public OverrideNameWirelet(String name) {
            this.name = ComponentSetup.checkComponentName(name); // throws IAE
        }

        /** {@inheritDoc} */
        @Override
        protected void onBuild(ComponentSetup c) {
            c.nameInitializedWithWirelet = true;
            c.name = name;
        }

        @Override
        public void onImageInstantiation(ComponentSetup c, ApplicationLaunchContext ic) {
            ic.name = name;
        }
    }
}
