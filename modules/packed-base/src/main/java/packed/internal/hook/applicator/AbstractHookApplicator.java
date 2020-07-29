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

import java.util.function.BiConsumer;

import app.packed.component.SingletonConfiguration;
import app.packed.hook.HookApplicator;
import packed.internal.component.PackedComponentConfigurationContext;

/**
 *
 */
abstract class AbstractHookApplicator<T> implements HookApplicator<T> {

    /** {@inheritDoc} */
    @Override
    public final <S> void onReady(SingletonConfiguration<?> cc, Class<S> sidecarType, BiConsumer<S, T> consumer) {
        // Must have an owner.... And then ComponentConfiguration must have the same owner....
        // And I guess access mode as well, owner, for example, bundle.getClass();
        // Maybe check against the same lookup object...
        // Owner_Type, Component_Instance_Type, Field, FunctionalType, AccessMode
        /// Maybe we can just check ComponentConfiguration.lookup == this.lookup
        /// I think we actually need to check this this way

        // TODO check instance component if instance field...
        PackedComponentConfigurationContext pcc = (PackedComponentConfigurationContext) cc;
        pcc.checkConfigurable();
        pcc.del.add(newAccessor(sidecarType, consumer));
    }

    protected abstract <S> DelayedAccessor newAccessor(Class<S> sidecarType, BiConsumer<S, T> consumer);
}
