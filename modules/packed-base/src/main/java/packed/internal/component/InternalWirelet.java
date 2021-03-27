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

import app.packed.component.Component;
import app.packed.component.Wirelet;

/** A type of wirelet where its logic is directly embedded in the wirelet. */
public abstract class InternalWirelet extends Wirelet {

    abstract void onBuild(ComponentSetup c);

    void onImageInstantiation(ComponentSetup c, PackedInitializationContext ic) {
        throw new IllegalArgumentException("The wirelet {" + getClass().getSimpleName() + "} cannot be specified when instantiating an image");
    }

    /** A wirelet that will perform a given action once the component has been fully wired. */
    public static final class OnWireActionWirelet extends InternalWirelet {

        /** The action to perform on the component after it has been fully wired. */
        private final Consumer<? super Component> action;

        public OnWireActionWirelet(Consumer<? super Component> action) {
            this.action = requireNonNull(action, "action is null");
        }

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        void onBuild(ComponentSetup component) {
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
        void onBuild(ComponentSetup c) {
            c.nameInitializedWithWirelet = true;
            c.name = name;
        }

        @Override
        void onImageInstantiation(ComponentSetup c, PackedInitializationContext ic) {
            ic.name = name;
        }
    }
}
