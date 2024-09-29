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
package internal.app.packed.context;

import java.util.function.Consumer;

import app.packed.context.Context;
import app.packed.context.ContextTemplate;
import app.packed.extension.Extension;
import internal.app.packed.extension.ExtensionModel;
import internal.app.packed.util.types.TypeVariableExtractor;

/** Implementation of {@link ContextTemplate}. */
public record PackedContextTemplate(Class<? extends Extension<?>> extensionClass, Class<? extends Context<?>> contextClass,
        Class<? extends Context<?>> contextImplementationClass, boolean isHidden, boolean bindAsConstant) implements ContextTemplate {

    /** A ContextTemplate class to Extension class mapping. */
    private final static ClassValue<Class<? extends Extension<?>>> TYPE_VARIABLE_EXTRACTOR = new ClassValue<>() {

        /** A type variable extractor. */
        private static final TypeVariableExtractor EXTRACTOR = TypeVariableExtractor.of(Context.class);

        /** {@inheritDoc} */
        @Override
        protected Class<? extends Extension<?>> computeValue(Class<?> type) {
            return ExtensionModel.extractE(EXTRACTOR, type);
        }
    };

    public ContextTemplate.Descriptor descriptor() {
        throw new UnsupportedOperationException();
    }

    public ContextTemplate configure(Consumer<? super Configurator> configure) {
        PackedContextTemplateConfigurator c = new PackedContextTemplateConfigurator(this);
        configure.accept(c);
        return c.t;
    }

    public static ContextTemplate of(boolean isHidden, Class<? extends Context<?>> contextClass, Class<? extends Context<?>> implementation) {
        Class<? extends Extension<?>> c = PackedContextTemplate.TYPE_VARIABLE_EXTRACTOR.get(contextClass); // checks same module
        // check implementation is same class or implement contextclass
        return new PackedContextTemplate(c, contextClass, implementation, isHidden, false);
    }

    /**
     * @param contextClass2
     * @param configurator
     * @return
     */
    public static ContextTemplate of(Class<? extends Context<?>> contextClass, Consumer<? super Configurator> configurator) {
        Class<? extends Extension<?>> c = PackedContextTemplate.TYPE_VARIABLE_EXTRACTOR.get(contextClass); // checks same module
        PackedContextTemplate t = new PackedContextTemplate(c, contextClass, contextClass, false, false);
        return t.configure(configurator);
    }

    static class PackedContextTemplateConfigurator implements ContextTemplate.Configurator {

        public PackedContextTemplate t;

        public PackedContextTemplateConfigurator(PackedContextTemplate t) {
            this.t = t;
        }

        /** {@inheritDoc} */
        @Override
        public Configurator hidden() {
            t = new PackedContextTemplate(t.extensionClass, t.contextClass, t.contextImplementationClass, true, t.bindAsConstant);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Configurator implementationClass(Class<? extends Context<?>> implementationClass) {
            // TODO check subclass
            t = new PackedContextTemplate(t.extensionClass, t.contextClass, implementationClass, t.isHidden, t.bindAsConstant);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Configurator bindAsConstant() {
            t = new PackedContextTemplate(t.extensionClass, t.contextClass, t.contextImplementationClass, t.isHidden, true);
            return this;
        }
    }
}
