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
package app.packed.component;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;

import app.packed.bean.BeanInstallationException;
import app.packed.bean.BeanTrigger.OnAnnotatedVariable;
import internal.app.packed.bean.scanning.IntrospectorOnVariable;
import internal.app.packed.bean.sidehandle.SidehandleBeanHandle;
import internal.app.packed.extension.base.BaseExtensionBeanIntrospector;
import internal.app.packed.lifecycle.LifecycleOperationHandle.AbstractInitializingOperationHandle;

/**
 * Can be used to annotated injectable parameters into a guest bean.
 *
 * @see SidehandleContext
 * @see OnComponentGuestLifecycle
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
@OnAnnotatedVariable(introspector = SidehandleBindingBeanIntrospector.class, requiresContext = SidehandleContext.class)
public @interface SidehandleBinding {

    Kind value();

    public enum Kind {
        HANDLE_CONSTANT, HANDLE_COMPUTED_CONSTANT, OPERATION_INVOKER, FROM_CONTEXT;
    }
}

final class SidehandleBindingBeanIntrospector extends BaseExtensionBeanIntrospector {

    @SuppressWarnings("unchecked")
    @Override
    public void onAnnotatedVariable(Annotation annotation, OnVariable v) {
        @SuppressWarnings("rawtypes")
        Optional<SidehandleBeanHandle> sideHandle = beanHandle(SidehandleBeanHandle.class);

        if (sideHandle.isEmpty()) {
            throw new BeanInstallationException(SidehandleBinding.class.getSimpleName() + " can only be used on sidehandle beans");
        }
        if (v.operationHandle(AbstractInitializingOperationHandle.class).isEmpty()) {
            throw new BeanInstallationException(SidehandleBinding.class.getSimpleName() + " can only be used on Factory, Inject, Initialize methods" + beanClass());
        }

        sideHandle.get().onInject(this, (SidehandleBinding) annotation, (IntrospectorOnVariable) v);
    }
}