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

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import app.packed.bean.BeanHandle;
import app.packed.bean.BeanInstaller;
import app.packed.bean.BeanLifetime;
import app.packed.bean.BeanMirror;
import app.packed.bean.SidebeanAttachment;
import app.packed.bean.SidebeanBinding;
import app.packed.bean.SidebeanConfiguration;
import app.packed.binding.Key;
import internal.app.packed.bean.scanning.IntrospectorOnVariable;
import internal.app.packed.invoke.BeanLifecycleSupport;
import internal.app.packed.lifecycle.InvokableLifecycleOperationHandle;
import internal.app.packed.lifecycle.LifecycleOperationHandle;
import internal.app.packed.service.util.ServiceMap;

/**
 *
 */
// SideBeanHandle -has many> SideBeanInstance
public class SidebeanHandle<T> extends BeanHandle<SidebeanConfiguration<T>> {

    public final ServiceMap<PackedSidebeanBinding> bindings = new ServiceMap<>();

    public final Set<Key<?>> injectionSites = new HashSet<>();

    private ArrayList<PackedSidebeanAttachment> usage = new ArrayList<>();

    public SidebeanInvokerModel sim;

    /**
     * @param installer
     */
    public SidebeanHandle(BeanInstaller installer) {
        super(installer);
    }

    public Stream<PackedSidebeanAttachment> attachments() {
        return usage.stream();
    }

    public SidebeanAttachment attachTo(PackedSidebeanAttachment usage) {
        // For example, for a cron
        usage.bean.sideBeanAttachments.add(usage);

        // Im guessing we need to make room for it no matter what
        if (usage.bean.beanKind == BeanLifetime.SINGLETON) {
            usage.lifetimeStoreIndex = usage.bean.container.lifetime.store.add(usage);
        }
        attachTolifecycle(usage);

        return usage;
    }


    private void attachTolifecycle(PackedSidebeanAttachment susage) {
        usage.add(susage);
        requireNonNull(susage);
        for (List<InvokableLifecycleOperationHandle<LifecycleOperationHandle>> l : susage.sidebean.operations.lifecycleHandles.values()) {
            for (InvokableLifecycleOperationHandle<LifecycleOperationHandle> loh : l) {
                InvokableLifecycleOperationHandle<LifecycleOperationHandle> newh = new InvokableLifecycleOperationHandle<LifecycleOperationHandle>(loh.handle, susage);
                susage.bean.operations.addLifecycleHandle(newh);
                BeanLifecycleSupport.addLifecycleHandle(newh);
            }
        }
    }

    public void checkUnusued() {
        if (!usage.isEmpty()) {
            throw new IllegalStateException();
        }
    }

    @Override
    protected SidebeanConfiguration<T> newBeanConfiguration() {
        return new SidebeanConfiguration<>(this);
    }

    @Override
    protected BeanMirror newBeanMirror() {
        return super.newBeanMirror();
    }

    @Override
    protected void onConfigured() {
        if (!bindings.keySet().equals(injectionSites)) {
            throw new IllegalStateException(bindings.keySet() + "  " + injectionSites);
        }

        super.onConfigured();
    }

    /**
     * @param annotation
     * @param v
     */
    public void onInject(SidebeanBinding annotation, IntrospectorOnVariable v) {
        // This method is invoked, before #bindings is populated, so we cannot make any checks here
        Key<?> key = v.toKey();
        injectionSites.add(key);
        v.bindSidebeanBinding(key, this);
    }
}
