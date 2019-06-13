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

import java.util.function.BiConsumer;

import app.packed.component.ComponentConfiguration;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.MethodDescriptor;
import packed.internal.util.StringFormatter;

/** Takes care of component methods annotated with {@link Main}. */
final class MainExtensionHookGroup extends ExtensionHookGroup<ComponentExtension, MainExtensionHookGroup.Builder> {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        onAnnotatedMethodDescription(Main.class, (b, m) -> b.add(m));
    }

    /** {@inheritDoc} */
    @Override
    public Builder newBuilder(Class<?> componentType) {
        return new Builder();
    }

    static class Builder implements ExtensionHookGroup.Builder<ComponentExtension> {

        MethodDescriptor method;

        private void add(MethodDescriptor method) {
            if (this.method != null) {
                throw new InvalidDeclarationException("A component of the type '" + StringFormatter.format(method.getDeclaringClass())
                        + "' defined more than one method annotated with @" + Main.class.getSimpleName() + ", Methods = "
                        + StringFormatter.formatShortWithParameters(this.method) + ", " + StringFormatter.formatShortWithParameters(method));
            }
            this.method = method;
        }

        /** {@inheritDoc} */
        @Override
        public BiConsumer<ComponentConfiguration, ComponentExtension> build() {
            return (c, e) -> {};
        }
    }
}
