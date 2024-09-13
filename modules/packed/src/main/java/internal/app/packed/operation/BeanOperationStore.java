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
package internal.app.packed.operation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import app.packed.lifetime.LifecycleOperationMirror;
import app.packed.operation.OperationHandle;
import internal.app.packed.bean.BeanLifecycleOperation;
import internal.app.packed.bean.BeanLifecycleOrder;
import internal.app.packed.service.ServiceProviderSetup;
import internal.app.packed.util.LazyNamer;

/** This class manages all operations declared by a bean. */
public final class BeanOperationStore implements Iterable<OperationSetup> {

    /** Operations declared by the bean. */
    private final ArrayList<OperationSetup> all = new ArrayList<>();

    /**
     * All lifecycle operations for the bean. Is initially unsorted as operations can be added in any order. But in the end
     * the list will be sorted in the order of execution. With {@link app.packed.lifetime.RunState#INITIALIZING} lifecycle
     * operations first, and {@link app.packed.lifetime.RunState#STOPPING} lifecycle operations at the end.
     */
    public final ArrayList<BeanLifecycleOperation> lifecycleOperations = new ArrayList<>();

    /**
     * The unique name of every operation.
     * <p>
     * We map a operation setup to a string instead of the other way around. So that
     * {@link app.packed.operation.OperationMirror#name()} is fast.
     * <p>
     * This is lazily generated primarily for use in mirrors. We generate it lazily because calculating the unique names of
     * operations is actually a bit time consuming.
     */
    volatile Map<OperationSetup, String> operationNames;

    public boolean providingOperationsVisited;

    /** A list of services provided by the bean, used for circular dependency checks. */
    public final List<ServiceProviderSetup> serviceProviders = new ArrayList<>();

    void add(OperationSetup os) {
        all.add(os);
    }

    public void addLifecycleOperation(BeanLifecycleOrder runOrder, OperationHandle<?> operation) {
        lifecycleOperations.add(new BeanLifecycleOperation(runOrder, operation));
        operation.specializeMirror(LifecycleOperationMirror::new);
    }

    public OperationSetup first() {
        return all.get(0);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<OperationSetup> iterator() {
        return all.iterator();
    }

    /**
     * <p>
     * We lazily calculate
     *
     * @return a map of operation to
     */
    public Map<OperationSetup, String> operationNames() {
        Map<OperationSetup, String> m = operationNames;
        // operationNames is only valid as
        if (m == null || m.size() != operationNames.size()) {
            m = operationNames = LazyNamer.calculate(all, OperationSetup::namePrefix);
        }
        return m;
    }

    public Stream<OperationSetup> stream() {
        return all.stream();
    }
}
