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
package app.packed.bean;

import app.packed.bean.hooks.BeanClass;
import app.packed.bean.hooks.BeanField;
import app.packed.bean.hooks.BeanField.AnnotatedWithHook;
import app.packed.bean.hooks.BeanInfo;
import app.packed.bean.hooks.BeanMethod;
import app.packed.bean.hooks.BeanVariable;
import app.packed.operation.dependency.DependencyProvider;

/**
 *
 */
public class BeanScanner {
    
    public void onClass(BeanClass clazz) {}

    public void onDependencyProvider(DependencyProvider providr) {}

    public void onEnd(BeanInfo beanInfo) {}

    /**
     * A callback method that is invoked for any field on a newly added bean where the field:
     * 
     * is annotated with an annotation that itself is annotated with {@link BeanField.AnnotatedWithHook} and where
     * {@link AnnotatedWithHook#extension()} matches the type of this extension.
     * <p>
     * This method is never invoked more than once for a single field for any given extension. Even if there are multiple
     * matching hook annotations on the same field. This method will only be called once for the field.
     * 
     * @param bf
     *            the bean field
     * @see BeanField.AnnotatedWithHook
     */
    public void onField(BeanField bf) {}

    public void onMethod(BeanMethod method) {
        throw new UnsupportedOperationException(/* method,hooks not handled on getClass()... */);
    }

    /**
     * 
     * This method is always called before
     * 
     * @param beanInfo
     *            information about the bean
     */
    // What happens if we install an extension that adds a bean for scanning that uses the same extension???
    // I think we should wait with the scanning??? IDK
    public void onNew(BeanInfo beanInfo) {}

    public void onVariable(BeanVariable variable) {}
    
}
