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
package internal.app.packed.lifetime;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;

import app.packed.lifecycle.LifecycleKind;
import app.packed.lifetime.BeanLifetimeMirror;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.PackedBeanInstaller;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/** The lifetime of a bean whose lifetime is independent of its container. */
public final class BeanLifetimeSetup implements LifetimeSetup {

    /** A MethodHandle for invoking {@link LifetimeMirror#initialize(LifetimeSetup)}. */
    private static final MethodHandle MH_BEAN_LIFETIME_MIRROR_INITIALIZE = LookupUtil.findVirtual(MethodHandles.lookup(), BeanLifetimeMirror.class,
            "initialize", void.class, BeanLifetimeSetup.class);

    /** The single bean this lifetime contains. */
    public final List<BeanSetup> beans;

    /** */
    public final List<FuseableOperation> lifetimes;

    public BeanLifetimeSetup(BeanSetup bean, PackedBeanInstaller installer) {
        if (installer.template.bot() == null) {
            this.lifetimes = List.of();
        } else {
            this.lifetimes = FuseableOperation.of(List.of(installer.template.bot()));
        }
        this.beans = List.of(bean);
    }

    /** {@inheritDoc} */
    @Override
    public LifecycleKind lifetimeKind() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public List<FuseableOperation> lifetimes() {
        return lifetimes;
    }

    /** {@return a mirror that can be exposed to end-users.} */
    @Override
    public BeanLifetimeMirror mirror() {
        BeanLifetimeMirror mirror = new BeanLifetimeMirror();

        // Initialize BeanLifetimeMirror by calling BeanLifetimeMirror#initialize(BeanLifetimeSetup)
        try {
            MH_BEAN_LIFETIME_MIRROR_INITIALIZE.invokeExact(mirror, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return mirror;
    }

    /** {@inheritDoc} */
    @Override
    public ContainerLifetimeSetup parent() {
        return beans.get(0).container.lifetime;
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> resultType() {
        return void.class;
    }
}
