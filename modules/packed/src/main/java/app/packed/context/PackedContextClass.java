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
package app.packed.context;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import app.packed.extension.Extension;
import internal.app.packed.container.ExtensionModel;
import internal.app.packed.util.types.TypeVariableExtractor;

/**
 *
 */
public record PackedContextClass(Class<? extends Extension<?>> extensionClass, Class<? extends Context<?>> contextClass, List<Class<?>> invocationArguments)
        implements ContextTemplate {

    /** A ContextTemplate class to Extension class mapping. */
    final static ClassValue<Class<? extends Extension<?>>> TYPE_VARIABLE_EXTRACTOR = new ClassValue<>() {

        /** A type variable extractor. */
        private static final TypeVariableExtractor EXTRACTOR = TypeVariableExtractor.of(ContextTemplate.class);

        /** {@inheritDoc} */
        @Override
        protected Class<? extends Extension<?>> computeValue(Class<?> type) {
            return ExtensionModel.extractE(EXTRACTOR, type);
        }
    };

    /** {@inheritDoc} */
    @Override
    public ContextTemplate withArgument(Class<?> argument) {
        requireNonNull(argument, "argument is null");
        ArrayList<Class<?>> l = new ArrayList<>(invocationArguments);
        l.add(argument);
        return new PackedContextClass(extensionClass, contextClass, List.copyOf(l));
    }
}
