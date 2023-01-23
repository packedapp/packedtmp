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
package internal.app.packed.container;

import app.packed.container.Wirelet;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.lifetime.runtime.ApplicationInitializationContext;

/**
 * A special wirelet for internal usage where the logic of the wirelet is embedded directly into the wirelet.
 */
public abstract class InternalWirelet extends Wirelet {

    /**
     * Checks that the specified component is the root component (container) of an application.
     * 
     * @param component
     *            the component to check
     * @throws IllegalArgumentException
     *             if the specified component is not the root component of an application
     * @return the application of the component (for method chaining)
     */
    protected final ApplicationSetup checkIsApplication(ContainerSetup component) {
        ApplicationSetup application = component.application;
        if (application.container != component) {
            throw new IllegalArgumentException("This wirelet can only be specified when wiring the root container of an application, wirelet = " + this);
        }
        return application;
    }

//    protected <T> PackedApplicationDriver<T> onApplicationDriver(PackedApplicationDriver<T> driver) {
//        // Ide'en er at vi kan lave en ny application driver.. Hvor vi apply'er settings..
//        return driver;
//    }

    /**
     * Invoked by the runtime when the component is initially wired at build-time.
     * 
     * @param component
     *            the component that is being wired
     */
    protected abstract void onBuild(ContainerSetup component);

    public void onImageInstantiation(ContainerSetup component, ApplicationInitializationContext context) {
        throw new IllegalArgumentException(
                "The wirelet {" + getClass().getSimpleName() + "} must be specified at build-time. It cannot be specified when instantiating an image");
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

//    /** A wirelet that will perform a given action once the component has been fully wired. */
//    public static final class OnWireActionWirelet extends InternalWirelet {
//
//        /** The action to perform on each in-scope component after it has been fully wired. */
//        private final Consumer<? super ComponentMirror> action;
//
//        /** The scope of the wirelet */
//        @Nullable
//        private final ComponentScope scope;
//
//        public OnWireActionWirelet(Consumer<? super ComponentMirror> action, @Nullable ComponentScope scope) {
//            this.action = requireNonNull(action, "action is null");
//            this.scope = scope;
//        }
//
//        /** {@inheritDoc} */
//        @SuppressWarnings({ "unchecked", "rawtypes" })
//        @Override
//        protected void onBuild(ContainerSetup component) {
//            // Hmm. Vi vil nok snare have en liste nu, hvis vi har mere end 2
//            Consumer<? super ComponentMirror> existing = component.onWireAction;
//            if (existing == null) {
//                component.onWireAction = action;
//            } else {
//                component.onWireAction = existing.andThen((Consumer) action);
//            }
//        }
//    }

    /** A wirelet that will set the name of the component. Used by {@link Wirelet#named(String)}. */
    public static final class OverrideNameWirelet extends InternalWirelet {

        /** The (validated) name to override with. */
        private final String name;

        /**
         * Creates a new name wirelet
         * 
         * @param name
         *            the name to override any existing container name with
         */
        public OverrideNameWirelet(String name) {
            this.name = NameCheck.checkComponentName(name); // throws IAE
        }

        /** {@inheritDoc} */
        @Override
        protected void onBuild(ContainerSetup c) {
            c.isNameInitializedFromWirelet = true;
            c.name = name; // has already been validated
        }

        /** {@inheritDoc} */
        @Override
        public void onImageInstantiation(ContainerSetup c, ApplicationInitializationContext ic) {
            ic.name = name;
        }
    }
}
