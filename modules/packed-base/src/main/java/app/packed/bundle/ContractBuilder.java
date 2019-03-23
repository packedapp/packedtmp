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
package app.packed.bundle;

import static java.util.Objects.requireNonNull;

import app.packed.bundle.Contract.ContractFragment;
import app.packed.inject.ServiceContract;

/**
 *
 *
 *
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong> If multiple threads access a contract builder
 * concurrently, and at least one of the threads modifies the contract builder structurally, it <i>must</i> be
 * synchronized externally.
 */
// See AbstractInternalBuilder
public final class ContractBuilder {

    ServiceContract services;

    /**
     * Creates a new contract from this builder.s
     * 
     * @return a new contract
     */
    public Contract build() {
        throw new UnsupportedOperationException();

    }

    /**
     * Returns a object that can be used
     * 
     * @return a service contract
     */
    public ServiceContract services() {
        ServiceContract s = services;
        return s == null ? services = ServiceContract.EMPTY : s;
    }

    @SuppressWarnings("unchecked")
    <T> T with(Class<T> type) {
        requireNonNull(type, "type is null");
        if (type == ServiceContract.class) {
            return (T) services();
        }
        throw new UnsupportedOperationException();
    }

    // Spoergsmaalet er om vi skal kunne fjerne ting....
    // F.eks. et requirement, det er jo primaert tiltaenkt hvis vi begynder noget med versioner....
    // Noget andet
    // Det virkede ikke rigtig sammen med ServiceConfiguration,

    // ContractBuilder b = c.toBuilder();
    // b.services().addRequires(String.class);
    // b.build();

    // Og Maybe top class ContractFragment + ContractFragment.Builder
    static abstract class ContractBuilderFragment {

        protected abstract ContractFragment build();
    }
}
