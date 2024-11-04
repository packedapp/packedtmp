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
package app.packed.lifetime;

import java.util.stream.Stream;

import app.packed.bean.BeanHandle;
import app.packed.bean.BeanMirror;

/**
 * <p>
 * A lifetime bean is always {@link BeanMirror#owner() owned} by an extension. It is not possible for users to register
 * lifetime beans.
 */
// Do we have any other types of beans, whose instances can be used multiple places?
// Or are lifetime beans the only ones? What about host interfaces.
public final class LifetimeBeanMirror extends BeanMirror {

    /**
     * @param handle
     */
    public LifetimeBeanMirror(BeanHandle<?> handle) {
        super(handle);
    }

    /** {@return a stream of all lifetimes this bean is used in} */
    public Stream<LifetimeMirror> lifetimes() {
        throw new UnsupportedOperationException();
    }
}
