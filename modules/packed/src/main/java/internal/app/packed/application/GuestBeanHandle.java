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
package internal.app.packed.application;

import java.util.Map;
import java.util.Set;

import app.packed.application.ApplicationMirror;
import app.packed.bean.Bean;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanInstaller;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanIntrospector.OnVariable;
import app.packed.bean.BeanLifetime;
import app.packed.binding.Key;
import app.packed.binding.Variable;
import app.packed.component.guest.ComponentHostConfiguration;
import app.packed.component.guest.ComponentHostContext;
import app.packed.context.Context;
import app.packed.operation.Op;
import app.packed.operation.Op1;
import app.packed.runtime.ManagedLifecycle;
import app.packed.service.ServiceLocator;
import internal.app.packed.bean.PackedBeanTemplate;
import internal.app.packed.build.AuthoritySetup;
import internal.app.packed.context.PackedComponentHostContext;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.lifecycle.LifecycleOperationHandle.FactoryOperationHandle;
import internal.app.packed.lifecycle.runtime.ApplicationLaunchContext;

/**
 *
 */
public final class GuestBeanHandle extends BeanHandle<ComponentHostConfiguration<?>> {

    /** A bean template for the guest bean. */
    public static final PackedBeanTemplate APPLICATION_GUEST_BEAN_TEMPLATE = PackedBeanTemplate.builder(BeanLifetime.UNMANAGED).initialization(
            c -> c.withReturnTypeDynamic().withRaw().withContext(ApplicationLaunchContext.CONTEXT_TEMPLATE).withContext(PackedComponentHostContext.TEMPLATE))
            .build();

    static final Set<Key<?>> KEYS = Set.of(Key.of(ApplicationMirror.class), Key.of(String.class), Key.of(ManagedLifecycle.class), Key.of(ServiceLocator.class));

    public static final PackedComponentHostContext DEFAULT = new PackedComponentHostContext(KEYS);

    /**
     * @param installer
     */
    private GuestBeanHandle(BeanInstaller installer) {
        super(installer);
    }

    // Called when service resolving contexts...
    public Map<Class<? extends Context<?>>, Set<Key<?>>> dynamicContexts() {
        return Map.of();
    }

    public FactoryOperationHandle factory() {
       return (FactoryOperationHandle) lifecycleInvokers().get(0);
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
        } else if (va.rawType().equals(ComponentHostContext.class)) {
            v.bindOp(new Op1<ApplicationLaunchContext, ComponentHostContext>(_ -> PackedComponentHostContext.DEFAULT) {});
        } else {
            throw new UnsupportedOperationException("Unknown Container Guest Service " + va.rawType());
        }
    }

    public PackedComponentHostContext toContext() {
        return DEFAULT;
    }

    public static GuestBeanHandle install(PackedApplicationTemplate<?> template, ExtensionSetup installingExtension, AuthoritySetup<?> owner) {
        // Create a new installer for the bean
        BeanInstaller installer = APPLICATION_GUEST_BEAN_TEMPLATE.newInstaller(installingExtension, owner);

        // Create the bean
        Bean<?> bean = template.op() == null ? Bean.of(template.guestClass()) : Bean.of((Op<?>) template.op());

        GuestBeanHandle h = installer.install(bean, GuestBeanHandle::new);
        return h;
    }
}
