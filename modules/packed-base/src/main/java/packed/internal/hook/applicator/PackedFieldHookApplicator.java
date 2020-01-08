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
import java.lang.reflect.Field;
import java.util.function.BiConsumer;

import app.packed.hook.AnnotatedFieldHook;

/** A hook applicator for a field */
public final class PackedFieldHookApplicator<T> extends AbstractHookApplicator<T> {

    /** The field we want to apply the operator on. */
    public final Field field;

    public final MethodHandle mh;

    /** The operator to apply. */
    public final VarOperator<T> operator;

    public PackedFieldHookApplicator(AnnotatedFieldHook<?> hook, VarOperator<T> operator, Field field) {
        if (operator.isSimpleGetter()) {
            mh = hook.getter();
        } else {
            mh = hook.setter();
        }
        this.field = requireNonNull(field);
        this.operator = requireNonNull(operator);
    }

    /** {@inheritDoc} */
    @Override
    protected <S> DelayedAccessor newAccessor(Class<S> sidecarType, BiConsumer<S, T> consumer) {
        return new DelayedAccessor.SidecarFieldDelayerAccessor(this, sidecarType, consumer);
    }
}
