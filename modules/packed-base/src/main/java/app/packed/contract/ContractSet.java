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
package app.packed.contract;

import static java.util.Objects.requireNonNull;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.StreamSupport;

/**
 * A set of contracts guaranteed to contain no more then a single contract of a particular type.
 */
public final class ContractSet extends Contract implements Iterable<Contract> {
    public static final ContractSet EMPTY = new ContractSet(new IdentityHashMap<>());

    // Do we want to maintain some kind of order????
    // SimpleName???
    final IdentityHashMap<Class<?>, Contract> contracts;

    ContractSet(IdentityHashMap<Class<?>, Contract> contracts) {
        this.contracts = requireNonNull(contracts);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object other) {
        if (other instanceof ContractSet) {
            return contracts.equals(((ContractSet) other).contracts);
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return contracts.values().hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<Contract> iterator() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return contracts.values().toString();
    }

    public Set<Class<? extends Contract>> types() {
        throw new UnsupportedOperationException();
    }

    public <T extends Contract> T use(Class<T> contractType) {
        return use(this, contractType);
    }

    public ContractSet with(Contract... contracts) {
        // If a contract of the specific type already exists it is replaced
        // Check that we dont add ContractSet
        throw new UnsupportedOperationException();
    }

    public static ContractSet of(Contract... contracts) {
        requireNonNull(contracts, "contracts is null");
        IdentityHashMap<Class<?>, Contract> m = new IdentityHashMap<>();
        for (Contract contract : contracts) {
            if (contract instanceof ContractSet) {
                for (Contract cc : ((ContractSet) contract)) {
                    m.put(cc.getClass(), cc);
                }
            } else {
                m.put(contract.getClass(), contract);
            }
        }
        // Fail if the same contract is add multiple times... nah just overwrite.
        // otherwise we need to do it in with to.

        // If we add contract set we extract all
        return new ContractSet(m);
    }

    public static ContractSet of(Iterable<? extends Contract> contracts) {
        return of(StreamSupport.stream(contracts.spliterator(), false).toArray(Contract[]::new));
    }
}
