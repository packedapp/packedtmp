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
package app.packed.service.mirror;

import app.packed.operation.OperationHandle;
import app.packed.operation.OperationMirror;
import app.packed.service.ServiceNamespaceMirror;

/**
 *
 */
public class ServiceNamespaceProvideOperationMirror extends OperationMirror {

    /**
     * @param handle
     */
    public ServiceNamespaceProvideOperationMirror(OperationHandle<?> handle) {
        super(handle);
    }

    /** {@inheritDoc} */
    public ServiceNamespaceMirror namespace() {
        return null;
    }

    ServiceProviderMirror.FromNamespace providerMirror() {
        throw new UnsupportedOperationException();
    }
}
