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
package app.packed.component;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 *
 */
public interface ComponentDriver<C> {

    public interface Option {

        /**
         * The component the driver will be a container.
         * <p>
         * A container that is a component cannot be sourced??? Yes It can... It can be the actor system
         * 
         * @return stuff
         */
        // Or Maybe
        static Option container() {
            throw new UnsupportedOperationException();
        }

        // The parent + the driver
        //
        static Option validateWiring(BiConsumer<Component, WireableComponentDriver<?>> validator) {
            throw new UnsupportedOperationException();
        }

        static Option validateParentIsContainer() {
            return validateParent(c -> c.attribute(Component.PROPERTIES).contains(ComponentProperty.CONTAINER),
                    "This component can only be wired to a container");
        }

        static Option validateParent(Predicate<? super Component> validator, String msg) {
            return validateWiring((c, d) -> {
                if (validator.test(c)) {
                    throw new IllegalArgumentException(msg);
                }
            });
        }

        // Option serviceable()
        // Hmm Maaske er alle serviceable.. Og man maa bare lade vaere
        // at expose funktionaliteten.
    }
}
