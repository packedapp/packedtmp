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
package app.packed.service.sandbox;

import app.packed.bean.BeanHandle;
import app.packed.bean.BeanVariable;
import app.packed.operation.OperationHandle;
import app.packed.util.Key;

// ServiceDomain? Something where keys are unique

// Features:
//// PerBean, PerContainer, PerApplication
//// Allow Producers that are not consumed? (could be a simple verifyNoUnConsumed());
//// Do we support dynamic services? Key->SomethingBindable.bind
//// Support hiraki? fx Foerst leder vi i bean map, saa i container map hvis ikke fundet

/**
 * Ideen er at goere det ket at manage services for fx {@link CodeGenerated}
 */
// Consuming Annotation

// Maybe it is an abstract class
// I'm thinking about errors messages

// Ideen er lidt sat vi hjaelper lidt paa med Key baseret service domain
public class LocalServiceMap<V> {

    // consumers

    // Vi har fundet en binding som vi gerne vil binde til en service

    // Must be resolvable to a key
    public void consume(BeanVariable variable) {}

    // Producing multiple services with the same key is not supported

    public void produceConstant(Key<?> key, Object constant) {}

    public void produceOptionalService(Key<?> key, OperationHandle handle) {}

    public void produce(Key<?> key, OperationHandle handle) {}

    public <T> void produceBean(Key<T> key, BeanHandle<T> handle) {}

    public void resolve() {}
}
