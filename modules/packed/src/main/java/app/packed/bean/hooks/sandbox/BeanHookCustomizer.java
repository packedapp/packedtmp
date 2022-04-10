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
package app.packed.bean.hooks.sandbox;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;

import app.packed.bean.hooks.scrap.ScrapBeanMethod;

/**
 *
 */
public abstract class BeanHookCustomizer {

    protected abstract void build();

    protected final void mapMethodAnnotation(Class<? extends Annotation> annotation, Class<ScrapBeanMethod> methodHook) {
        throw new UnsupportedOperationException();
    }
    
    //// Using another implementation of Hooks...
    // clearMethodAnnotationHooksFor(Schedule.class)
    // mapMethodAnnotation(..., NewHook.class)
    
    // restrictTo(AbstractUser.class).mapMethodAnnotation(...)
}

class Usage extends BeanHookCustomizer {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        mapMethodAnnotation(Documented.class, null);
    }
}