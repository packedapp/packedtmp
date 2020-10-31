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
package packed.internal.inject.service.build;

import java.lang.invoke.MethodHandle;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.config.ConfigSite;
import app.packed.inject.ServiceExtension;
import packed.internal.inject.Dependant;
import packed.internal.inject.service.runtime.DelegatingRuntimeService;
import packed.internal.inject.service.runtime.RuntimeService;
import packed.internal.inject.service.runtime.ServiceInstantiationContext;
import packed.internal.util.MethodHandleUtil;

/**
 * A build entry representing an exported service. Entries at runtime has never any reference to how (or if) they where
 * exported.
 */
public final class RekeyBuildtimeService extends BuildtimeService {

    /** The actual entry that is exported. Is initially null for keyed exports, until it is resolved. */
    public final BuildtimeService entryToRekey;

    /**
     * Exports an entry via its key.
     * 
     * @param s
     *            the injector configuration this node is being added to
     * @param configSite
     *            the configuration site of the exposure
     * @see ServiceExtension#export(Class)
     * @see ServiceExtension#export(Key)
     */
    public RekeyBuildtimeService(BuildtimeService s, Key<?> key, ConfigSite configSite) {
        super(configSite, key);
        this.entryToRekey = s;
    }

    @Override
    @Nullable
    public Dependant dependant() {
        return entryToRekey.dependant();
    }

    @Override
    public MethodHandle dependencyAccessor() {
        MethodHandle mh = entryToRekey.dependencyAccessor();
        return MethodHandleUtil.castReturnType(mh, key().rawType());
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConstant() {
        return entryToRekey.isConstant();
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeService newRuntimeNode(ServiceInstantiationContext context) {
        return new DelegatingRuntimeService(this, entryToRekey.toRuntimeEntry(context));
    }
}
