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
package internal.app.packed.lifecycle.lifetime;

import internal.app.packed.bean.BeanSetup;
import internal.app.packed.util.accesshelper.BeanLifetimeAccessHandler;
import sandbox.app.packed.lifetime.BeanLifetimeMirror;

/** The lifetime of a bean whose lifetime is independent of the lifecycle of other beans. */
public final class BeanLifetimeSetup implements LifetimeSetup {

    /** The single bean this lifetime contains. */
    public final BeanSetup bean;

    public BeanLifetimeSetup(BeanSetup bean) {
        this.bean = bean;
    }

    /** {@return a mirror that can be exposed to end-users.} */
    @Override
    public BeanLifetimeMirror mirror() {
        return BeanLifetimeAccessHandler.instance().newBeanLifetimeMirror(this);
    }

    /** {@inheritDoc} */
    @Override
    public ContainerLifetimeSetup parent() {
        return bean.container.lifetime;
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> resultType() {
        return void.class;
    }
}
