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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.function.Supplier;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanLocal;
import app.packed.bean.BeanLocalAccessor;
import app.packed.bean.BeanMirror;
import app.packed.extension.BeanIntrospector;
import app.packed.util.Nullable;
import internal.app.packed.component.PackedComponentLocal;
import internal.app.packed.component.PackedLocalKeyAndSource;
import internal.app.packed.util.LookupUtil;
import sandbox.extension.bean.BeanHandle;

/**
 *
 */
public final class PackedBeanLocal<T> extends PackedComponentLocal<BeanLocalAccessor, T> implements BeanLocal<T> {

    /** A handle that can access BeanConfiguration#handle. */
    private static final VarHandle VH_BEAN_MIRROR_TO_SETUP = LookupUtil.findVarHandle(MethodHandles.lookup(), BeanMirror.class, "bean", BeanSetup.class);

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
    protected PackedLocalKeyAndSource extract(BeanLocalAccessor accessor) {
        requireNonNull(accessor, "accessor is null");
        return switch (accessor) {
        case BeanConfiguration bc -> BeanSetup.crack(bc);
        case PackedBeanElement bc -> bc.bean();
        case BeanHandle<?> bc -> BeanSetup.crack(bc);
        case BeanIntrospector bc -> BeanSetup.crack(bc);
        case BeanMirror bc -> (BeanSetup) VH_BEAN_MIRROR_TO_SETUP.get(bc);
        default -> throw new Error();
        };
    }
}
