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
package internal.app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;

import app.packed.bean.BeanLocal;
import app.packed.bean.BeanLocalAccessor;
import app.packed.util.Nullable;
import internal.app.packed.build.PackedBuildLocal;
import internal.app.packed.build.PackedLocalMap.KeyAndLocalMapSource;

/** Implementation of BeanLocal. */
public final class PackedBeanLocal<T> extends PackedBuildLocal<BeanLocalAccessor, T> implements BeanLocal<T> {

    /**
     * @param initialValueSupplier
     */
    public PackedBeanLocal(@Nullable Supplier<? extends T> initialValueSupplier) {
        super(initialValueSupplier);
    }

    /**
     * Extracts the actual bean setup from the specified accessor.
     *
     * @param accessor
     *            the accessor to extract from
     * @return the extracted bean
     */
    @Override
    protected KeyAndLocalMapSource extract(BeanLocalAccessor accessor) {
        requireNonNull(accessor, "accessor is null");
        return BeanSetup.crack(accessor);
    }
}
