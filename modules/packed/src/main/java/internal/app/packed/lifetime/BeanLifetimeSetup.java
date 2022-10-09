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

import app.packed.lifetime.BeanLifetimeMirror;
import app.packed.lifetime.LifetimeMirror;
import internal.app.packed.bean.BeanSetup;

/**
 *
 */
public final class BeanLifetimeSetup extends LifetimeSetup {

    /** The single bean this lifetime contains. */
    // We know from installer whether or not we are lazy
    public final BeanSetup bean;

    /**
     * @param parent
     */
    public BeanLifetimeSetup(ContainerLifetimeSetup parent, BeanSetup bean) {
        super(parent);
        this.bean = bean;
    }

    /** {@inheritDoc} */
    @Override
    LifetimeMirror mirror0() {
        return new BeanLifetimeMirror();
    }
}
