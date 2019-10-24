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
package packed.internal.container.extension;

import java.lang.annotation.Annotation;

import app.packed.container.Extension;
import app.packed.container.UseExtension;

/**
 *
 */
public interface ActivatorMap {
    ActivatorMap DEFAULT = new DefaultActivatorMap();

    Class<? extends Extension>[] onAnnotatedType(Class<? extends Annotation> annotationType);

    Class<? extends Extension>[] onAnnotatedMethod(Class<? extends Annotation> annotationType);

    Class<? extends Extension>[] onAnnotatedField(Class<? extends Annotation> annotationType);
}

class DefaultActivatorMap implements ActivatorMap {
    /** A cache of any extensions a particular annotation activates. */
    static final ClassValue<Class<? extends Extension>[]> EXTENSION_ACTIVATORS = new ClassValue<>() {

        @Override
        protected Class<? extends Extension>[] computeValue(Class<?> type) {
            UseExtension ae = type.getAnnotation(UseExtension.class);
            return ae == null ? null : ae.value();
        }
    };

    @Override
    public Class<? extends Extension>[] onAnnotatedType(Class<? extends Annotation> annotationType) {
        return EXTENSION_ACTIVATORS.get(annotationType);
    }

    @Override
    public Class<? extends Extension>[] onAnnotatedMethod(Class<? extends Annotation> annotationType) {
        return EXTENSION_ACTIVATORS.get(annotationType);
    }

    @Override
    public Class<? extends Extension>[] onAnnotatedField(Class<? extends Annotation> annotationType) {
        return EXTENSION_ACTIVATORS.get(annotationType);
    }
}