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

import static java.util.Objects.requireNonNull;

import app.packed.bean.BeanKind;
import app.packed.lifetime.BeanLifetimeMirror;
import app.packed.lifetime.LifetimeMirror;
import internal.app.packed.bean.BeanSetup;

/** The lifetime of a bean that is instantiated independently of the container in which it lives. */
public final class BeanLifetimeSetup extends LifetimeSetup {

    /** The single bean this lifetime contains. */
    public final BeanSetup bean;

    public BeanLifetimeSetup(ContainerLifetimeSetup container, BeanSetup bean) {
        super(container);
        this.bean = requireNonNull(bean);
    }

    public boolean isLazy() {
        return bean.beanKind == BeanKind.LAZY;
    }

    /** {@inheritDoc} */
    @Override
    LifetimeMirror mirror0() {
        return new BeanLifetimeMirror();
    }
}
