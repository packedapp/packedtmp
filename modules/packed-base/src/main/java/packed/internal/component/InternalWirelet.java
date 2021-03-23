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

/**
 *
 */
// Tror meningen er at smide ind i .component igen...
public abstract class InternalWirelet extends Wirelet {

    protected abstract void firstPass(ComponentSetup c);

    /** A wirelet that will set the name of the container. Used by {@link Wirelet#named(String)}. */
    public static final class FailOnFirstPass extends InternalWirelet {


        @Override
        protected void firstPass(ComponentSetup component) {
            throw new Error();
        }
    }
    
    /** A wirelet that will set the name of the container. Used by {@link Wirelet#named(String)}. */
    public static final class OnWireCallbackWirelet extends InternalWirelet {

        private final Consumer<? super Component> action;

        public OnWireCallbackWirelet(Consumer<? super Component> action) {
            this.action = requireNonNull(action, "action is null");
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected void firstPass(ComponentSetup component) {
            Consumer<? super Component> existing = component.onWire;
            if (existing == null) {
                component.onWire = action;
            } else {
                component.onWire = existing.andThen((Consumer) action);
            }
        }
    }

    /** A wirelet that will set the name of the container. Used by {@link Wirelet#named(String)}. */
    public static final class SetComponentNameWirelet extends InternalWirelet {

        /** The (checked) name to override with. */
        public final String name;

        /**
         * Creates a new option
         * 
         * @param name
         *            the name to override any existing container name with
         */
        public SetComponentNameWirelet(String name) {
            this.name = checkName(name);
        }

        /**
         * Checks the name of the component.
         * 
         * @param name
         *            the name to check
         * @return the name if valid
         */
        public static String checkName(String name) {
            requireNonNull(name, "name is null");
            if (name != null) {

            }
            return name;
        }

        @Override
        protected void firstPass(ComponentSetup c) {
            c.nameState = ComponentSetup.NAME_INITIALIZED_WITH_WIRELET;
            c.name = name;
        }
    }
}
