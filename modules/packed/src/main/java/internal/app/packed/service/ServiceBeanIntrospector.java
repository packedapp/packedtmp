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
package internal.app.packed.service;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;

import app.packed.bean.BeanInstallationException;
import app.packed.bean.BeanKind;
import app.packed.bean.scanning.BeanIntrospector;
import app.packed.binding.Key;
import app.packed.build.BuildException;
import app.packed.extension.BaseExtension;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTemplate;
import app.packed.service.Export;
import app.packed.service.Provide;
import internal.app.packed.binding.BindingAccessor.FromOperationResult;
import internal.app.packed.extension.PackedBeanIntrospector;
import internal.app.packed.operation.OperationSetup;

/**
 *
 */
public final class ServiceBeanIntrospector extends PackedBeanIntrospector<BaseExtension> {

    static final OperationTemplate OPERATION_TEMPLATE = OperationTemplate.defaults().withReturnTypeDynamic();

    /**
     * Handles {@link Inject}.
     *
     * {@inheritDoc}
     */
    @Override
    public void onAnnotatedField(Annotation annotation, OnField onField) {
        if (!(annotation instanceof Provide)) {
            super.onAnnotatedField(annotation, onField);
        }

        if (!Modifier.isStatic(onField.modifiers())) {
            if (beanKind() != BeanKind.CONTAINER) {
                throw new BuildException("Not okay)");
            }
        }

        // Checks that it is a valid key
        Key<?> key = onField.toKey();

        OperationSetup operation = OperationSetup.crack(onField.newGetOperation(OPERATION_TEMPLATE).install(OperationHandle::new));
        bean().serviceNamespace().provideService(key, operation, new FromOperationResult(operation));
    }


    /**
     * Handles {@link Provide} and {@link Export}.
     *
     * {@inheritDoc}
     */
    @Override
    public void onAnnotatedMethod(Annotation annotation, BeanIntrospector.OnMethod method) {
        if (annotation instanceof Provide) {
            if (!Modifier.isStatic(method.modifiers())) {
                if (beanKind() != BeanKind.CONTAINER) {
                    throw new BeanInstallationException("Not okay)");
                }
            }

            // Checks that it is a valid key
            Key<?> key = method.toKey();

            OperationSetup operation = OperationSetup.crack(method.newOperation(OPERATION_TEMPLATE).install(OperationHandle::new));
            bean().serviceNamespace().provideService(key, operation, new FromOperationResult(operation));
        } else if (annotation instanceof Export) {
            if (!Modifier.isStatic(method.modifiers())) {
                if (beanKind() != BeanKind.CONTAINER) {
                    throw new BeanInstallationException("Not okay)");
                }
            }
            // Checks that it is a valid key
            Key<?> key = method.toKey();

            OperationSetup operation = OperationSetup.crack(method.newOperation(OPERATION_TEMPLATE).install(OperationHandle::new));
            bean().serviceNamespace().export(key, operation);
        }
    }
}
