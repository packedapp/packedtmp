/*
 * Copyright (c) 2026 Kasper Nielsen.
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
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanInstallationException;
import app.packed.bean.BeanInstaller;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanIntrospector.OnVariable;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanMirror;
import app.packed.binding.Key;
import app.packed.binding.Variable;
import app.packed.component.Sidehandle;
import app.packed.component.SidehandleBeanConfiguration;
import app.packed.component.SidehandleBinding;
import app.packed.component.SidehandleContext;
import app.packed.lifecycle.runtime.ManagedLifecycle;
import app.packed.operation.Op1;
import app.packed.service.ServiceLocator;
import internal.app.packed.application.PackedApplicationTemplate;
import internal.app.packed.bean.PackedBeanInstaller;
import internal.app.packed.bean.PackedBeanTemplate;
import internal.app.packed.bean.scanning.IntrospectorOnVariable;
import internal.app.packed.build.AuthoritySetup;
import internal.app.packed.context.PackedComponentHostContext;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.invoke.BeanLifecycleSupport;
import internal.app.packed.invoke.SidehandleInvokerModel;
import internal.app.packed.lifecycle.InvokableLifecycleOperationHandle;
import internal.app.packed.lifecycle.LifecycleOperationHandle;
import internal.app.packed.lifecycle.LifecycleOperationHandle.FactoryOperationHandle;
import internal.app.packed.lifecycle.runtime.ApplicationLaunchContext;
import internal.app.packed.operation.PackedOperationTemplate;
import internal.app.packed.service.util.ServiceMap;

/**
 *
 */
// SideBeanHandle -has many> SideBeanInstance
public class SidehandleBeanHandle<T> extends BeanHandle<SidehandleBeanConfiguration<T>> {

    // --- Fields from GuestBeanHandle ---
    static final Set<Key<?>> KEYS = Set.of(Key.of(ApplicationMirror.class), Key.of(String.class), Key.of(ManagedLifecycle.class), Key.of(ServiceLocator.class));

    public static final PackedComponentHostContext DEFAULT = new PackedComponentHostContext(KEYS);

    // --- Original SidehandleBeanHandle fields ---
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
        if (usage.bean.beanKind == BeanKind.SINGLETON) {
            usage.lifetimeStoreIndex = usage.bean.container.lifetime.store.add(usage);
        }
        attachTolifecycle(usage);

        return usage;
    }

    private void attachTolifecycle(PackedSidehandle susage) {
        requireNonNull(susage);
        sidehandles.add(susage);
        for (List<InvokableLifecycleOperationHandle<LifecycleOperationHandle>> l : susage.sidehandleBean.operations.lifecycleHandles.values()) {
            for (InvokableLifecycleOperationHandle<LifecycleOperationHandle> loh : l) {
                InvokableLifecycleOperationHandle<LifecycleOperationHandle> newh = new InvokableLifecycleOperationHandle<LifecycleOperationHandle>(loh.handle,
                        susage);
                susage.bean.operations.addLifecycleHandle(newh);
                BeanLifecycleSupport.addLifecycleHandle(newh);
            }
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

    private HashMap<Key<?>, SidehandleBinding.Kind> kinds = new HashMap<>();

    /**
     * @param annotation
     * @param v
     */
    public void onInject(BeanIntrospector<?> introspector, SidehandleBinding annotation, IntrospectorOnVariable v) {
        SidehandleBinding.Kind kind = annotation.value();
        Key<?> key = v.toKey();

        SidehandleBinding.Kind existingKind = kinds.put(key, kind);
        if (existingKind != null && existingKind != kind) {
            throw new BeanInstallationException("Conflicting SidehandleBinding kinds for key " + key
                + ": existing=" + existingKind + ", new=" + kind);
        }


        // For FROM_CONTEXT, use resolve method
        if (kind == SidehandleBinding.Kind.FROM_CONTEXT) {
            resolve(introspector, v);
            return;
        }

        PackedSidehandleBinding binding;
        if (kind == SidehandleBinding.Kind.CONSTANT || kind == SidehandleBinding.Kind.COMPUTED_CONSTANT) {
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

    // --- Methods from GuestBeanHandle ---

    public FactoryOperationHandle factory() {
        return (FactoryOperationHandle) lifecycleInvokers().getFirst();
    }

    public void resolve(BeanIntrospector<?> i, OnVariable v) {
        Variable va = v.variable();

        // AssignableTo in case of
        if (va.rawType().equals(ApplicationMirror.class)) {
            v.bindOp(new Op1<ApplicationLaunchContext, ApplicationMirror>(a -> a.mirror()) {});
        } else if (va.rawType().equals(String.class)) {
            v.bindOp(new Op1<ApplicationLaunchContext, String>(a -> a.name()) {});
        } else if (va.rawType().equals(ManagedLifecycle.class)) {
            v.bindOp(new Op1<ApplicationLaunchContext, ManagedLifecycle>(a -> a.runner.runtime) {});
        } else if (va.rawType().equals(ServiceLocator.class)) {
            v.bindOp(new Op1<ApplicationLaunchContext, ServiceLocator>(a -> a.serviceLocator()) {});
        } else if (va.rawType().equals(SidehandleContext.class)) {
            v.bindOp(new Op1<ApplicationLaunchContext, SidehandleContext>(_ -> PackedComponentHostContext.DEFAULT) {});
        } else {
            throw new UnsupportedOperationException("Unknown Container Guest Service " + va.rawType());
        }
    }

    public SidehandleContext toContext() {
        return DEFAULT;
    }

    public static SidehandleBeanHandle<?> installApplication(PackedApplicationTemplate<?> template, ExtensionSetup installingExtension, AuthoritySetup<?> owner) {
        // Create a new installer for the bean
        PackedBeanTemplate beanTemplate = new PackedBeanTemplate(BeanKind.UNMANAGED,
                PackedOperationTemplate.DEFAULTS.withRaw().withContext(ApplicationLaunchContext.class));
        BeanInstaller installer = new PackedBeanInstaller(beanTemplate, installingExtension, owner);

        return installer.install(template.bean(), SidehandleBeanHandle::new);
    }
}
