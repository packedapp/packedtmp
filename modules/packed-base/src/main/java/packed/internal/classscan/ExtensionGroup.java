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
package packed.internal.classscan;

import java.util.function.BiConsumer;

import app.packed.component.ComponentConfiguration;
import app.packed.container.ActivateExtension;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ExtensionGroupConfigurator;

/**
 *
 */

// Spoergsmaalet er her Lookup objektet....

// Hvis det er med i CompiledExt compilere vi den en gang per module sikkert (for
final class ExtensionGroup implements BiConsumer<ContainerConfiguration, ComponentConfiguration> {

    static final ExtensionGroup EMPTY = new ExtensionGroup(ExtensionGroupConfigurator.class);

    static final ClassValue<ExtensionGroup> FOR_ANNOTATION = new ClassValue<>() {

        @Override
        protected ExtensionGroup computeValue(Class<?> type) {
            ActivateExtension ae = type.getAnnotation(ActivateExtension.class);
            return ae == null ? ExtensionGroup.EMPTY : FOR_CLASS.get(ae.annotationType());
        }
    };

    private static final ClassValue<ExtensionGroup> FOR_CLASS = new ClassValue<>() {

        @Override
        protected ExtensionGroup computeValue(Class<?> type) {
            return new ExtensionGroup(type);
        }
    };

    private ExtensionGroup(Class<?> cl) {

        // forName
        // invoke configure();
    }

    /** {@inheritDoc} */
    @Override
    public void accept(ContainerConfiguration t, ComponentConfiguration u) {
        // TODO Auto-generated method stub

    }

}
