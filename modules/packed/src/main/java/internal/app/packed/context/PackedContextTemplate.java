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

    /** {@inheritDoc} */
    @Override
    public ContextTemplate withHidden() {
        return new PackedContextTemplate(extensionClass, contextClass, contextImplementationClass, true, bindAsConstant);
    }

    public static ContextTemplate of(Class<? extends Context<?>> contextClass) {
        Class<? extends Extension<?>> c = PackedContextTemplate.TYPE_VARIABLE_EXTRACTOR.get(contextClass);
        return new PackedContextTemplate(c, contextClass, contextClass, false, false);
    }

    /** {@inheritDoc} */
    @Override
    public ContextTemplate withImplementation(Class<? extends Context<?>> implementationClass) {
        // TODO check subclass
        return new PackedContextTemplate(extensionClass, contextClass, implementationClass, isHidden, bindAsConstant);
    }

    /** {@inheritDoc} */
    @Override
    public ContextTemplate withBindAsConstant() {
        return new PackedContextTemplate(extensionClass, contextClass, contextImplementationClass, isHidden, true);

    }

}
