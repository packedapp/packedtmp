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

import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.Set;

import app.packed.application.ApplicationMirror;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanInstaller;
import app.packed.bean.BeanKind;
import app.packed.bean.scanning.BeanIntrospector;
import app.packed.binding.BindableVariable;
import app.packed.binding.Key;
import app.packed.binding.Variable;
import app.packed.component.guest.ComponentHostConfiguration;
import app.packed.component.guest.ComponentHostContext;
import app.packed.context.Context;
import app.packed.context.ContextTemplate;
import app.packed.operation.Op;
import app.packed.operation.Op1;
import app.packed.operation.OperationTemplate;
import app.packed.runtime.ManagedLifecycle;
import app.packed.service.ServiceLocator;
import internal.app.packed.bean.PackedBeanTemplate;
import internal.app.packed.context.PackedComponentHostContext;
import internal.app.packed.lifecycle.lifetime.runtime.ApplicationLaunchContext;

/**
 *
 */
public class GuestBeanHandle extends BeanHandle<ComponentHostConfiguration<?>> {


    static final ContextTemplate GB_HIT = ContextTemplate.of(ComponentHostContext.class,
            c -> c.implementationClass(PackedComponentHostContext.class).bindAsConstant());

    static final OperationTemplate GB_CON = OperationTemplate.raw()
            .reconfigure(c -> c.inContext(ApplicationLaunchContext.CONTEXT_TEMPLATE).inContext(GB_HIT).returnTypeObject());

    public static final PackedBeanTemplate GUEST_BEAN_TEMPLATE = new PackedBeanTemplate(BeanKind.UNMANAGED).withOperationTemplate(GB_CON);

    public static MethodHandle installGuestBean(PackedApplicationTemplate<?> template, BeanInstaller installer) {
        if (template.guestClass() == Void.class) {
            return null;
        }
        GuestBeanHandle h;
        if (template.op() == null) {
            h = installer.install(template.guestClass(), GuestBeanHandle::new);
        } else {
            h = installer.install((Op<?>) template.op(), GuestBeanHandle::new);
        }

        MethodHandle m = h.lifetimeOperations().get(0).methodHandle();
        m = m.asType(m.type().changeReturnType(Object.class));
        return m;
    }

    /**
     * @param installer
     */
    public GuestBeanHandle(BeanInstaller installer) {
        super(installer);
    }

    static final Set<Key<?>> KEYS = Set.of(Key.of(ApplicationMirror.class), Key.of(String.class), Key.of(ManagedLifecycle.class), Key.of(ServiceLocator.class));

    public static final PackedComponentHostContext DEFAULT = new PackedComponentHostContext(KEYS);

    public PackedComponentHostContext toContext() {
        return DEFAULT;
    }

    // Called when service resolving contexts...
    public Map<Class<? extends Context<?>>, Set<Key<?>>> dynamicContexts() {
        return Map.of();
    }

    public void resolve(BeanIntrospector i, BindableVariable v) {
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
            v.bindOp(new Op1<ApplicationLaunchContext, ComponentHostContext>(a -> PackedComponentHostContext.DEFAULT) {});
        } else {
            throw new UnsupportedOperationException("Unknown Container Guest Service " + va.rawType());
        }
    }
}
