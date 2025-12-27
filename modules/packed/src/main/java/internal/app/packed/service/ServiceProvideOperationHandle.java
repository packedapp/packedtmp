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

import app.packed.bean.BeanIntrospector.OnField;
import app.packed.bean.BeanIntrospector.OnMethod;
import app.packed.binding.Key;
import app.packed.operation.OperationConfiguration;
import app.packed.operation.OperationInstaller;
import app.packed.operation.OperationTemplate;
import app.packed.service.Provide;
import internal.app.packed.binding.BindingProvider.FromEmbeddedOperation;
import internal.app.packed.extension.base.BaseExtensionOperationHandle;

/**
 *
 */
public class ServiceProvideOperationHandle extends BaseExtensionOperationHandle<OperationConfiguration>{

    static final OperationTemplate OPERATION_TEMPLATE = OperationTemplate.defaults().withReturnTypeDynamic();

    /**
     * @param installer
     */
    public ServiceProvideOperationHandle(OperationInstaller installer) {
        super(installer);
    }

    public static void install(Provide annotation, OnMethod method) {
        // Checks that it is a valid key
        Key<?> key = method.toKey();

        ServiceProvideOperationHandle oh = method.newOperation(OPERATION_TEMPLATE).install(ServiceProvideOperationHandle::new);
        oh.bean().serviceNamespace().provideService(key, oh, new FromEmbeddedOperation(oh.operation()));
    }

    public static void install(Provide annotation, OnField onField) {

        // Checks that it is a valid key
        Key<?> key = onField.toKey();

        ServiceProvideOperationHandle oh = onField.newGetOperation(OPERATION_TEMPLATE).install(ServiceProvideOperationHandle::new);
        oh.bean().serviceNamespace().provideService(key, oh, new FromEmbeddedOperation(oh.operation()));
    }

}
