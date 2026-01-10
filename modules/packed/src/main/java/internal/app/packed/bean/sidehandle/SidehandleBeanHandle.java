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
package internal.app.packed.bean.sidehandle;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import app.packed.bean.BeanHandle;
import app.packed.bean.BeanInstaller;
import app.packed.bean.BeanLifetime;
import app.packed.bean.BeanMirror;
import app.packed.binding.Key;
import app.packed.component.Sidehandle;
import app.packed.component.SidehandleBeanConfiguration;
import app.packed.component.SidehandleBinding;
import internal.app.packed.bean.scanning.IntrospectorOnVariable;
import internal.app.packed.invoke.BeanLifecycleSupport;
import internal.app.packed.invoke.SidehandleInvokerModel;
import internal.app.packed.lifecycle.InvokableLifecycleOperationHandle;
import internal.app.packed.lifecycle.LifecycleOperationHandle;
import internal.app.packed.service.util.ServiceMap;

/**
 *
 */
// SideBeanHandle -has many> SideBeanInstance
public class SidehandleBeanHandle<T> extends BeanHandle<SidehandleBeanConfiguration<T>> {

    public final ServiceMap<PackedSidehandleBinding> bindings = new ServiceMap<>();

    private ArrayList<PackedSidehandle> sidehandles = new ArrayList<>();

    public SidehandleInvokerModel invokerModel;

    /**
     * @param installer
     */
    public SidehandleBeanHandle(BeanInstaller installer) {
        super(installer);
    }

    public Stream<PackedSidehandle> sidehandles() {
        return sidehandles.stream();
    }

    public Sidehandle attachTo(PackedSidehandle usage) {
        // For example, for a cron
        usage.bean.sideBeanAttachments.add(usage);

        // Im guessing we need to make room for it no matter what
        if (usage.bean.beanKind == BeanLifetime.SINGLETON) {
            usage.lifetimeStoreIndex = usage.bean.container.lifetime.store.add(usage);
        }
        attachTolifecycle(usage);

        return usage;
    }

    private void attachTolifecycle(PackedSidehandle susage) {
        requireNonNull(susage);
        sidehandles.add(susage);
        for (List<InvokableLifecycleOperationHandle<LifecycleOperationHandle>> l : susage.sidebean.operations.lifecycleHandles.values()) {
            for (InvokableLifecycleOperationHandle<LifecycleOperationHandle> loh : l) {
                InvokableLifecycleOperationHandle<LifecycleOperationHandle> newh = new InvokableLifecycleOperationHandle<LifecycleOperationHandle>(loh.handle, susage);
                susage.bean.operations.addLifecycleHandle(newh);
                BeanLifecycleSupport.addLifecycleHandle(newh);
            }
        }
    }

    public void checkUnusued() {
        if (!sidehandles.isEmpty()) {
            throw new IllegalStateException();
        }
    }

    @Override
    protected SidehandleBeanConfiguration<T> newBeanConfiguration() {
        return new SidehandleBeanConfiguration<>(this);
    }

    @Override
    protected BeanMirror newBeanMirror() {
        return super.newBeanMirror();
    }

    @Override
    protected void onConfigured() {
        super.onConfigured();
    }

    /**
     * @param annotation
     * @param v
     */
    public void onInject(SidehandleBinding annotation, IntrospectorOnVariable v) {
        Key<?> key = v.toKey();

        PackedSidehandleBinding binding;
        SidehandleBinding.Kind kind = annotation.value();
        if (kind == SidehandleBinding.Kind.HANDLE_CONSTANT || kind == SidehandleBinding.Kind.HANDLE_COMPUTED_CONSTANT) {
            binding = new PackedSidehandleBinding.Constant();
        } else if (kind == SidehandleBinding.Kind.OPERATION_INVOKER) {
            if (invokerModel != null) {
                throw new IllegalStateException("Only one operation invoker supported per sidebean");
            }
            SidehandleInvokerModel sim = invokerModel = SidehandleInvokerModel.of(key.rawType());
            sim.constructor(); // Awaits fix in module-tests
            binding = new PackedSidehandleBinding.Invoker(sim);
        } else {
            throw new IllegalStateException("Unknown kind: " + kind);
        }

        bindings.putIfAbsent(key, binding);
        v.bindSidebeanBinding(key, this);
    }
}
