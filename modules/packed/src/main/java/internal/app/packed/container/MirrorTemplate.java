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
package internal.app.packed.container;

import app.packed.base.Nullable;
import internal.app.packed.bean.BeanSetup;

/**
 *
 */
class MirrorTemplate {
    /**
     * The internal configuration of the bean we are mirroring. Is initially null but populated via
     * {@link #initialize(BeanSetup)}.
     */
    @Nullable
    private BeanSetup bean;

    /**
     * Create a new bean mirror.
     * <p>
     * Subclasses should have a single package-protected constructor.
     */
    public MirrorTemplate() {}

    /**
     * {@return the internal configuration of the bean we are mirroring.}
     * 
     * @throws IllegalStateException
     *             if {@link #initialize(BeanSetup)} has not been called previously.
     */
    private BeanSetup bean() {
        BeanSetup b = bean;
        if (b == null) {
            throw new IllegalStateException(
                    "Either this method has been called from the constructor of the mirror. Or the mirror has not yet been initialized by the runtime.");
        }
        return b;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object other) {
        return this == other || other instanceof MirrorTemplate m && bean() == m.bean();
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return bean().hashCode();
    }

    /**
     * Invoked by the runtime with the internal configuration of the bean to mirror.
     * 
     * @param bean
     *            the internal configuration of the bean to mirror
     */
    final void initialize(BeanSetup bean) {
        if (this.bean != null) {
            throw new IllegalStateException("This mirror has already been initialized.");
        }
        this.bean = bean;
    }
}
