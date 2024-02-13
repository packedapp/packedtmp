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
package sandbox.service;

import app.packed.extension.BindableVariable;
import app.packed.util.Key;
import sandbox.extension.bean.BeanHandle;
import sandbox.extension.operation.OperationHandle;

// ServiceDomain? Something where keys are unique

// Features:
//// PerBean, PerContainer, PerApplication
//// Allow Producers that are not consumed? (could be a simple verifyNoUnConsumed());
//// Do we support dynamic services? Key->SomethingBindable.bind
//// Support hiraki? fx Foerst leder vi i bean map, saa i container map hvis ikke fundet

// Paa BaseExtensionPoint
/**
 * Ideen er at goere det ket at manage services for fx {@link CodeGenerated}
 */
// Consuming Annotation

// Maybe it is an abstract class
// I'm thinking about errors messages

// Ideen er lidt sat vi hjaelper lidt paa med Key baseret service domain

// BaseExtensionPoint.newServiceDomain(xxx propertie);
public interface LocalServiceMap {

    // consumers

    // Vi har fundet en binding som vi gerne vil binde til en service

    // Must be resolvable to a key
    void bindInto(BindableVariable variable);
    void bindInto(Class<?> key, BindableVariable variable);
    void bindInto(Key<?> key, BindableVariable variable);

    // Producing multiple services with the same key is not supported

    void produceOptionalService(Key<?> key, OperationHandle handle);

    <T> void provideBeanInstance(Key<T> key, BeanHandle handle);

    <T> void provideConstant(Key<T> key, T constant);

    void provideOperation(Key<?> key, OperationHandle handle);

    void resolve();
}
