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
package internal.app.packed.bean;

import java.util.function.Supplier;

import app.packed.bean.BeanLocal;
import app.packed.bean.BeanLocal.Accessor;
import org.jspecify.annotations.Nullable;
import internal.app.packed.build.BuildLocalMap.BuildLocalSource;
import internal.app.packed.build.PackedBuildLocal;

/** Implementation of BeanLocal. */
public final class PackedBeanBuildLocal<T> extends PackedBuildLocal<Accessor, T> implements BeanLocal<T> {

    /**
     * @param initialValueSupplier
     */
    public PackedBeanBuildLocal(@Nullable Supplier<? extends T> initialValueSupplier) {
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
    protected BuildLocalSource extract(Accessor accessor) {
        return BeanSetup.crack(accessor);
    }
}
