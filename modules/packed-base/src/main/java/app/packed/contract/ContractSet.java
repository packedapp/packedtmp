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

/**
 *
 */
public final class ContractSet extends Contract implements Iterable<Contract> {
    public static final ContractSet EMPTY = new ContractSet();

    // Do we want to maintain some kind of order????
    // SimpleName???
    final IdentityHashMap<Class<?>, Contract> contracts = new IdentityHashMap<>();

    /** {@inheritDoc} */
    @Override
    protected boolean equalsTo(Contract other) {
        return contracts.equals(((ContractSet) other).contracts);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<Contract> iterator() {
        throw new UnsupportedOperationException();
    }

    public Set<Class<? extends Contract>> types() {
        throw new UnsupportedOperationException();
    }

    public ContractSet with(Contract... contracts) {
        // If a contract of the specific type already exists it is replaced
        // Check that we dont add ContractSet
        throw new UnsupportedOperationException();
    }

    public static ContractSet of(Contract... contracts) {
        requireNonNull(contracts, "contracts is null");
        IdentityHashMap<Class<?>, Contract> m = new IdentityHashMap<>();
        for (int i = 0; i < contracts.length; i++) {
            //
        }
        System.out.println(m);
        // Fail if the same contract is add multiple times... nah just overwrite.
        // otherwise we need to do it in with to.

        // If we add contract set we extract all
        throw new UnsupportedOperationException();
    }

    public static ContractSet of(Iterable<? extends Contract> contracts) {

        throw new UnsupportedOperationException();
    }

}
