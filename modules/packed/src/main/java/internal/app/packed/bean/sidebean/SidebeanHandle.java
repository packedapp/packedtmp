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
import app.packed.bean.BeanMirror;
import app.packed.bean.sidebean.SidebeanBinding;
import app.packed.bean.sidebean.SidebeanConfiguration;
import internal.app.packed.bean.scanning.IntrospectorOnVariable;
import internal.app.packed.invoke.BeanLifecycleSupport;
import internal.app.packed.lifecycle.LifecycleOperationHandle;
import internal.app.packed.lifecycle.InvokableLifecycleOperationHandle;
import internal.app.packed.service.util.ServiceMap;

/**
 *
 */
// SideBeanHandle -has many> SideBeanInstance
public class SidebeanHandle<T> extends BeanHandle<SidebeanConfiguration<T>> {

    private ArrayList<PackedSidebeanAttachment> usage = new ArrayList<>();

    public final ServiceMap<PackedSidebeanBinding> bindings = new ServiceMap<>();

    public final ServiceMap<IntrospectorOnVariable> injectionSites = new ServiceMap<>();

    /**
     * @param installer
     */
    public SidebeanHandle(BeanInstaller installer) {
        super(installer);
    }

    @Override
    protected void onConfigured() {
        if (!bindings.keySet().equals(injectionSites.keySet())) {
            throw new IllegalStateException(bindings.keySet() + "  " + injectionSites.keySet());
        }
        for (PackedSidebeanBinding psb : bindings) {
            if (psb instanceof PackedSidebeanBinding.SharedConstant sc) {
                injectionSites.get(sc.key()).bindConstant(sc.constant());
            }
        }

        super.onConfigured();
    }

    public void checkUnusued() {
        if (!usage.isEmpty()) {
            throw new IllegalStateException();
        }
    }

    public void addAttachment(PackedSidebeanAttachment susage) {
        usage.add(susage);

        for (List<InvokableLifecycleOperationHandle<LifecycleOperationHandle>> l : susage.sidebean.operations.lifecycleHandles.values()) {
            for (InvokableLifecycleOperationHandle<LifecycleOperationHandle> loh : l) {
                InvokableLifecycleOperationHandle<LifecycleOperationHandle> newh = new InvokableLifecycleOperationHandle<LifecycleOperationHandle>(loh.handle, susage);
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
    public void onInject(SidebeanBinding annotation, IntrospectorOnVariable v) {
        // This method is invoked, before #bindings is populated, so we cannot make any checks here
        injectionSites.put(v.toKey(), v);
    }
}
