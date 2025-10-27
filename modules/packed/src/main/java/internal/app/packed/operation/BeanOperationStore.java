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
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import app.packed.bean.BeanLifetime;
import app.packed.bean.BeanSourceKind;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.lifecycle.BeanLifecycleOperationHandle;
import internal.app.packed.lifecycle.InternalBeanLifecycleKind;
import internal.app.packed.service.ServiceProviderSetup.NamespaceServiceProviderHandle;
import internal.app.packed.util.CollectionUtil;
import internal.app.packed.util.LazyNamer;

/** This class manages all operations declared by a bean. */
public final class BeanOperationStore implements Iterable<OperationSetup> {

    /** Operations declared by the bean. */
    public final ArrayList<OperationSetup> all = new ArrayList<>();

    public CompoundOperation initialization;

    /**
     * All lifecycle operations for the bean. Is initially unsorted as operations can be added in any order. But in the end
     * the list will be sorted in the order of execution. With {@link app.packed.lifetime.RunState#INITIALIZING} lifecycle
     * operations first, and {@link app.packed.lifetime.RunState#STOPPING} lifecycle operations at the end.
     */
    public final EnumMap<InternalBeanLifecycleKind, List<BeanLifecycleOperationHandle>> lifecycleHandles = new EnumMap<>(InternalBeanLifecycleKind.class);

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
    public final List<NamespaceServiceProviderHandle> serviceProviders = new ArrayList<>();

    void add(OperationSetup os) {
        all.add(os);
    }

    public <T extends BeanLifecycleOperationHandle> void addLifecycleHandle(T handle) {
        lifecycleHandles.compute(handle.lifecycleKind, (_, v) -> {
            if (v == null) {
                return List.of(handle);
            } else {
                return CollectionUtil.copyAndAdd(v, handle);
            }
        });
        OperationSetup os = OperationSetup.crack(handle);
        if (handle.lifecycleKind == InternalBeanLifecycleKind.FACTORY) {
            BeanSetup bean = OperationSetup.crack(handle).bean;
            if (bean.beanKind == BeanLifetime.SINGLETON) {
                assert (bean.bean.beanSourceKind != BeanSourceKind.INSTANCE);

                bean.container.application.addCodegenAction(() -> {
                    os.codeHolder.setMethodHandle(os.codeHolder.newMethodHandle());
                });
            }
        } else {

            os.codeHolder.setMethodHandle(os.codeHolder.asMethodHandle());
        }
       // return handle;
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
