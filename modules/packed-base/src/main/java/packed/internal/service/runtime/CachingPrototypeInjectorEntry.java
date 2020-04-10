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
package packed.internal.service.runtime;

import app.packed.service.ProvideContext;
import app.packed.service.ServiceMode;
import packed.internal.service.build.ServiceExtensionInstantiationContext;
import packed.internal.service.build.service.ComponentFactoryBuildEntry;

/**
 *
 */
public class CachingPrototypeInjectorEntry<T> extends PrototypeInjectorEntry<T> {

    private T instance;

    /**
     * @param node
     */
    public CachingPrototypeInjectorEntry(ComponentFactoryBuildEntry<T> node, ServiceExtensionInstantiationContext context) {
        super(node, context);
    }

    @Override
    public T getInstance(ProvideContext site) {
        T i = instance;
        if (i == null) {
            i = instance = super.newInstance();
        }
        return i;
    }

    @Override
    public ServiceMode instantiationMode() {
        return super.instantiationMode();
    }
}
