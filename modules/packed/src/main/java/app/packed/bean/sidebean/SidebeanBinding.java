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
package app.packed.bean.sidebean;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.bean.BeanTrigger.OnAnnotatedVariable;
import internal.app.packed.bean.scanning.IntrospectorOnVariable;
import internal.app.packed.bean.sidebean.SidebeanHandle;
import internal.app.packed.extension.base.BaseExtensionBeanIntrospector;
import internal.app.packed.lifecycle.LifecycleOperationHandle.AbstractInitializingOperationHandle;

/**
 * Can be used to annotated injectable parameters into a guest bean.
 *
 * @see ComponentHostContext
 * @see OnComponentGuestLifecycle
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
@OnAnnotatedVariable(introspector = SidebeanInjectBeanIntrospector.class, requiresContext = SidebeanContext.class)
public @interface SidebeanBinding {}

final class SidebeanInjectBeanIntrospector extends BaseExtensionBeanIntrospector {

    @Override
    public void onAnnotatedVariable(Annotation annotation, OnVariable v) {
        if (bean().handle() instanceof SidebeanHandle<?> sh) {
            IntrospectorOnVariable iov = (IntrospectorOnVariable) v;
            // I probably want to use this for Guest as well
            if (!(iov.operation.handle() instanceof AbstractInitializingOperationHandle)) {
                throw new RuntimeException("" + iov.operation.bean.bean.beanClass);
            }

            sh.onInject((SidebeanBinding) annotation, iov);
        } else {
            // Can only be used on a side bean
            throw new RuntimeException();
        }
    }
}