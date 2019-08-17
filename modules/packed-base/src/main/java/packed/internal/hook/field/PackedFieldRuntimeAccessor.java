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
package packed.internal.hook.field;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import app.packed.component.ComponentConfiguration;
import app.packed.hook.DelayedHookOperator;
import packed.internal.container.AbstractComponentConfiguration;
import packed.internal.hook.PackedFieldOperation;

/**
 *
 */
public class PackedFieldRuntimeAccessor<T> implements DelayedHookOperator<T> {

    public final PackedFieldOperation<T> afo;

    public final MethodHandle mh;

    public final Field field;

    public PackedFieldRuntimeAccessor(MethodHandle mh, Field field, PackedFieldOperation<T> afo) {
        this.afo = requireNonNull(afo);
        this.mh = requireNonNull(mh);

        this.field = field;
    }

    /** {@inheritDoc} */
    @Override
    public void onReady(ComponentConfiguration cc, Consumer<T> consumer) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public <S> void onReady(ComponentConfiguration cc, Class<S> sidecarType, BiConsumer<S, T> consumer) {
        // TODO check instance component if instance field...
        AbstractComponentConfiguration pcc = (AbstractComponentConfiguration) cc;
        pcc.checkConfigurable();
        pcc.del.add(new DelayedAccessor.SidecarFieldDelayerAccessor(this, sidecarType, consumer));
    }
}
