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
import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanLocal;
import app.packed.bean.BeanLocalAccessor;
import app.packed.bean.BeanMirror;
import app.packed.extension.BeanIntrospector;
import app.packed.util.Nullable;
import internal.app.packed.component.PackedComponentLocal;
import internal.app.packed.component.PackedLocalMap;
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

    /** {@inheritDoc} */
    @Override
    public T get(BeanLocalAccessor accessor) {
        BeanSetup bean = crack(accessor);
        return bean.locals().get(this, bean);
    }

    @Override
    protected PackedLocalMap extract(BeanLocalAccessor accessor) {
        BeanSetup bean = crack(accessor);
        return bean.locals();
    }

    protected BeanSetup extractKey(BeanLocalAccessor accessor) {
        return crack(accessor);
    }


    /**
     * If a value is present, performs the given action with the value, otherwise does nothing.
     *
     * @param action
     *            the action to be performed, if a value is present
     * @throws NullPointerException
     *             if value is present and the given action is {@code null}
     */
    @Override
    public void ifBound(BeanLocalAccessor accessor, Consumer<? super T> action) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns whether or not a value has been bound to this local for the bean represented by the specified accessor.
     *
     * Was: Returns whether or not a value has been set or previously initialized in the specified accessor.
     *
     * @param accessor
     *            the bean local accessor
     * @return true if a value has been set or initialized, otherwise false
     *
     * @apiNote Calling this method will <strong>never</strong> initialize the value of the local even if a initial value
     *          supplier was specified when creating the local. As such this method rarely makes sense to call if an initial
     *          value supplier was specified when creating the local.
     */
    @Override
    public boolean isBound(BeanLocalAccessor accessor) {
        BeanSetup bean = crack(accessor);
        return bean.locals().isBound(this, bean);
    }

    /**
     * Returns whether or not a value has been bound or previously initialized in the specified accessor.
     *
     * @param accessor
     *            the bean local accessor
     * @param other
     *            the value to return if a value has not been bound previously
     * @return true if a value has been set or initialized, otherwise false
     *
     * @apiNote Calling this method will <strong>never</strong> initialize the value of the local even if a initial value
     *          supplier was specified when creating the local. As such this method rarely makes sense to call if an initial
     *          value supplier was specified when creating the local.
     */
    @Override
    public T orElse(BeanLocalAccessor accessor, T other) {
        BeanSetup bean = crack(accessor);
        return bean.locals().orElse(this, bean, other);
    }

    @Override
    public <X extends Throwable> T orElseThrow(BeanLocalAccessor accessor, Supplier<? extends X> exceptionSupplier) throws X {
        BeanSetup bean = crack(accessor);
        return bean.locals().orElseThrow(this, bean, exceptionSupplier);
    }

    // I think these are nice. We can use use for transformers. Add something for pre-transform.
    // Remove them for post, no need to keep them around
    @Override
    public T remove(BeanLocalAccessor accessor) {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the bound value of this local for the bean represented by the specified accessor.
     *
     * @param bean
     *            the bean
     * @param value
     *            the value to bind
     */
    @Override
    public void set(BeanLocalAccessor accessor, T value) {
        BeanSetup bean = crack(accessor);
        bean.locals().set(this, bean, value);
    }

    /**
     * Extracts the actual bean setup from the specified accessor.
     *
     * @param accessor
     *            the accessor to extract from
     * @return the extracted bean
     */
    private static BeanSetup crack(BeanLocalAccessor accessor) {
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
