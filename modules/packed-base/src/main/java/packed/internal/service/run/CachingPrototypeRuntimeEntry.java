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
package packed.internal.service.run;

import java.util.Map;

import app.packed.service.InstantiationMode;
import app.packed.service.PrototypeRequest;
import packed.internal.service.build.BuildEntry;
import packed.internal.service.build.service.ComponentFactoryBuildEntry;

/**
 *
 */
public class CachingPrototypeRuntimeEntry<T> extends PrototypeRuntimeEntry<T> {

    private T instance;

    /**
     * @param node
     */
    public CachingPrototypeRuntimeEntry(ComponentFactoryBuildEntry<T> node, Map<BuildEntry<?>, RuntimeEntry<?>> entries) {
        super(node, entries);
    }

    @Override
    public T get() {
        return getInstance(null);
    }

    @Override
    public T getInstance(PrototypeRequest site) {
        T i = instance;
        if (i == null) {
            i = instance = super.newInstance();
        }
        return i;
    }

    @Override
    public InstantiationMode instantiationMode() {
        // TODO Auto-generated method stub
        return super.instantiationMode();
    }

}
