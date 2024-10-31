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
import app.packed.binding.Key;
import app.packed.build.BuildException;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTemplate;
import app.packed.service.Export;
import app.packed.service.Provide;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.scanning.IntrospectorOnField;
import internal.app.packed.bean.scanning.IntrospectorOnMethod;
import internal.app.packed.binding.BindingAccessor.FromOperationResult;
import internal.app.packed.operation.OperationSetup;

/**
 *
 */

// Scanner:onInject   :onInject(BeanField, ? extends Annotation) <-- annotation
public final class ServiceAnnotationScanner {

    public static boolean testFieldAnnotation(IntrospectorOnField field, Annotation annotation) {

        if (annotation instanceof Provide) {
            BeanSetup bean = field.bean();

            Key<?> key = field.toKey();

            if (!Modifier.isStatic(field.modifiers())) {
                if (bean.beanKind != BeanKind.CONTAINER) {
                    throw new BuildException("Not okay)");
                }
            }

            OperationSetup operation = OperationSetup.crack(field.newGetOperation(TEMPLATE).install(OperationHandle::new));

            // Hmm, vi har jo slet ikke lavet namespacet endnu
            bean.serviceNamespace().provideService(bean, key, operation, new FromOperationResult(operation));
            return true;
        }
        return false;
    }

    static final OperationTemplate TEMPLATE = OperationTemplate.defaults().withReturnTypeDynamic();

    public static boolean testMethodAnnotation(IntrospectorOnMethod method, Annotation annotation) {
        if (annotation instanceof Provide) {
            if (!Modifier.isStatic(method.modifiers())) {
                if (method.bean().beanKind != BeanKind.CONTAINER) {
                    throw new BeanInstallationException("Not okay)");
                }
            }
            OperationSetup operation = OperationSetup.crack(method.newOperation(TEMPLATE).install(OperationHandle::new));
            method.bean().container.servicesMain().provideService(method.bean(), method.toKey(), operation, new FromOperationResult(operation));
        } else if (annotation instanceof Export) {
            if (!Modifier.isStatic(method.modifiers())) {
                if (method.bean().beanKind != BeanKind.CONTAINER) {
                    throw new BeanInstallationException("Not okay)");
                }
            }
            OperationSetup operation = OperationSetup.crack(method.newOperation(TEMPLATE).install(OperationHandle::new));
            method.bean().container.servicesMain().export(method.toKey(), operation);
        } else {
            return false;
        }
        return true;
    }

}
