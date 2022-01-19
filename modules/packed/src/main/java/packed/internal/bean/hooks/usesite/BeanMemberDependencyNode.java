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
package packed.internal.bean.hooks.usesite;

import app.packed.base.Nullable;
import packed.internal.bean.BeanSetup;
import packed.internal.bean.inject.DependencyNode;
import packed.internal.bean.inject.DependencyProducer;
import packed.internal.lifetime.PoolEntryHandle;

/**
 *
 */
public final class BeanMemberDependencyNode extends DependencyNode {

    public BeanMemberDependencyNode(BeanSetup source, UseSiteMemberHookModel smm, DependencyProducer[] dependencyProviders) {
        super(source, smm, dependencyProviders);
    }
    
    @Nullable
    protected PoolEntryHandle poolAccessor() {
        // buildEntry is null if it this Injectable is created from a source and not @AtProvides
        // In which case we store the build entry (if available) in the source instead
        if (service != null) {
            return service.accessor;
        }
        return null;
    }
}
