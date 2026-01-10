/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package app.packed.bean.sandbox;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanTrigger.OnAnnotatedVariable;
import app.packed.extension.BaseExtension;

/**
 * Can be used to annotated injectable parameters into a guest bean.
 *
 * @see SidehandleContext
 * @see OnComponentGuestLifecycle
 *
 * @see BeanHandle#attach(app.packed.operation.Op)
 *
 * @see app.packed.bean.scanning.BeanIntrospector#attachToBean(app.packed.operation.Op)
 * @see app.packed.bean.scanning.BeanIntrospector#attachToBeanIfAbsent(app.packed.operation.Op)
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
@OnAnnotatedVariable(introspector = FromBeanAttachmentBeanIntrospector.class)
public @interface FromBeanAttachment {}

final class FromBeanAttachmentBeanIntrospector extends BeanIntrospector<BaseExtension> {

    @Override
    public void onAnnotatedVariable(Annotation annotation, OnVariable variable) {
        // TODO: implement bean attachment binding
        super.onAnnotatedVariable(annotation, variable);
    }
}
