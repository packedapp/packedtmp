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
package packed.internal.container.extension.hook;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import app.packed.component.ComponentConfiguration;
import app.packed.container.extension.HookApplicator;
import packed.internal.container.AbstractComponentConfiguration;

/**
 *
 */
public class PackedFieldRuntimeAccessor<T> implements HookApplicator<T> {

    public final PackedFieldOperation<T> afo;

    public final MethodHandle mh;

    public final Field field;

    PackedFieldRuntimeAccessor(PackedAnnotatedFieldHook<?> hook, PackedFieldOperation<T> o) {
        this.mh = of(hook, o);
        this.field = hook.field;
        this.afo = o;
    }

    /** {@inheritDoc} */
    @Override
    public void onReady(ComponentConfiguration cc, Consumer<T> consumer) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public <S> void onReady(ComponentConfiguration cc, Class<S> sidecarType, BiConsumer<S, T> consumer) {
        // Must have an owner.... And then ComponentConfiguration must have the same owner....
        // And I guess access mode as well, owner, for example, bundle.getClass();
        // Maybe check against the same lookup object...
        // Owner_Type, Component_Instance_Type, Field, FunctionalType, AccessMode
        /// Maybe we can just check ComponentConfiguration.lookup == this.lookup
        /// I think we actually need to check this this way

        // TODO check instance component if instance field...
        AbstractComponentConfiguration pcc = (AbstractComponentConfiguration) cc;
        pcc.checkConfigurable();
        pcc.del.add(new DelayedAccessor.SidecarFieldDelayerAccessor(this, sidecarType, consumer));
    }

    private static MethodHandle of(PackedAnnotatedFieldHook<?> hook, PackedFieldOperation<?> o) {
        if (o.isSimpleGetter()) {
            return hook.getter();
        } else {
            return hook.setter();
        }
    }

}
