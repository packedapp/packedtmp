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
package internal.app.packed.lifecycle.lifetime.entrypoint;

import java.lang.annotation.Annotation;

import app.packed.bean.scanning.BeanIntrospector;
import app.packed.extension.BaseExtension;
import internal.app.packed.bean.scanning.IntrospectorOnMethod;
import internal.app.packed.extension.PackedBeanIntrospector;

/**
 *
 */
public final class MainBeanintrospector extends PackedBeanIntrospector<BaseExtension> {

    /**
     * {@inheritDoc}
     *
     * @see app.packed.lifetime.Main
     */
    @Override
    public void onAnnotatedMethod(Annotation annotation, BeanIntrospector.OnMethod method) {
        // Handles @Main
        if (EntryPointManager.testMethodAnnotation(extension(), isInApplicationLifetime(), (IntrospectorOnMethod) method, annotation)) {
            return;
        }

        super.onAnnotatedMethod(annotation, method);
    }

}
