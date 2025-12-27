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
package internal.app.packed.bean.sidebean;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import app.packed.bean.BeanHandle;
import app.packed.bean.BeanInstaller;
import app.packed.bean.BeanIntrospector.OnVariable;
import app.packed.bean.BeanMirror;
import app.packed.bean.sidebean.SidebeanConfiguration;
import app.packed.bean.sidebean.SidebeanInject;
import internal.app.packed.binding.BindingProvider;
import internal.app.packed.invoke.BeanLifecycleSupport;
import internal.app.packed.lifecycle.LifecycleOperationHandle;
import internal.app.packed.lifecycle.SomeLifecycleOperationHandle;
import internal.app.packed.service.util.ServiceMap;

/**
 *
 */
// SideBeanHandle -has many> SideBeanInstance
public class SidebeanHandle<T> extends BeanHandle<SidebeanConfiguration<T>> {

    private ArrayList<PackedSidebeanAttachment> usage = new ArrayList<>();

    public final ServiceMap<BindingProvider> providers = new ServiceMap<>();

    public final ServiceMap<OnVariable> injectionSites = new ServiceMap<>();

    /**
     * @param installer
     */
    public SidebeanHandle(BeanInstaller installer) {
        super(installer);
    }

    public void addAttachment(PackedSidebeanAttachment susage) {
        usage.add(susage);

        for (List<SomeLifecycleOperationHandle<LifecycleOperationHandle>> l : susage.sidebean.operations.lifecycleHandles.values()) {
            for (SomeLifecycleOperationHandle<LifecycleOperationHandle> loh : l) {
                SomeLifecycleOperationHandle<LifecycleOperationHandle> newh = new SomeLifecycleOperationHandle<LifecycleOperationHandle>(loh.handle, susage);
                susage.bean.operations.addLifecycleHandle(newh);
                BeanLifecycleSupport.addLifecycleHandle(newh);
            }
        }
    }

    public Stream<PackedSidebeanAttachment> attachments() {
        return usage.stream();
    }

    @Override
    protected SidebeanConfiguration<T> newBeanConfiguration() {
        return new SidebeanConfiguration<>(this);
    }

    @Override
    protected BeanMirror newBeanMirror() {
        return super.newBeanMirror();
    }

    /**
     * @param annotation
     * @param v
     */
    public void onInject(SidebeanInject annotation, OnVariable v) {
        injectionSites.put(v.toKey(), v);
    }
}
