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
package app.packed.namespace;

import app.packed.operation.OperationConfiguration;
import app.packed.operation.OperationHandle;

/**
 *
 */
//Could do runtime checks that NamespaceOperationConfiguration implies
// That a namespace has been set when installing the operation
public class NamespaceOperationConfiguration extends OperationConfiguration {

    /**
     * @param handle
     */
    public NamespaceOperationConfiguration(NamespaceHandle<?, ?> namespace, OperationHandle<?> installer) {
        super(installer);
    }
}