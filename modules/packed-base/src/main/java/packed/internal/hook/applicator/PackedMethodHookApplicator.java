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
package packed.internal.hook.applicator;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import app.packed.component.ComponentConfiguration;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.reflect.MethodOperator;

/**
 *
 */
public final class PackedMethodHookApplicator<T> extends AbstractHookApplicator<T> {

    /** The method we want to apply the operator on. */
    public final Method method;

    public final MethodHandle mh;

    /** The operator to apply. */
    public final MethodOperator<T> operator;

    public PackedMethodHookApplicator(AnnotatedMethodHook<?> hook, MethodOperator<T> operator, Method method) {
        this.operator = requireNonNull(operator);
        this.mh = hook.methodHandle();
        this.method = requireNonNull(method);
    }

    /** {@inheritDoc} */
    @Override
    protected <S> DelayedAccessor newAccessor(Class<S> sidecarType, BiConsumer<S, T> consumer) {
        return new DelayedAccessor.SidecarMethodDelayerAccessor(this, sidecarType, consumer);
    }

    /** {@inheritDoc} */
    @Override
    public void onReady(ComponentConfiguration<?> cc, Consumer<T> consumer) {
        throw new UnsupportedOperationException();
    }
}
