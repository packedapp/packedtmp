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
package packed.internal.host;

import java.util.concurrent.ConcurrentHashMap;

import app.packed.artifact.App;
import app.packed.base.ContractSet;
import app.packed.component.ComponentType;
import packed.internal.artifact.PackedInstantiationContext;
import packed.internal.component.AbstractComponent;
import packed.internal.component.AbstractComponentConfiguration;

/**
 *
 */
public class PackedHost extends AbstractComponent implements Host {

    // App is not a component, so can't really use children. Unless, we attach the artifact
    // to the component, which we probably should
    // We need some kind of wrapper, because we also want to support artifacts.
    // that are not apps
    final ConcurrentHashMap<String, App> apps = new ConcurrentHashMap<>();

    /**
     * @param configuration
     */
    PackedHost(AbstractComponent parent, AbstractComponentConfiguration configuration, PackedInstantiationContext ic) {
        super(parent, configuration, ic);
    }

    /** {@inheritDoc} */
    @Override
    public ComponentType type() {
        return ComponentType.HOST;
    }

    /** {@inheritDoc} */
    @Override
    public ContractSet contracts() {
        return ContractSet.EMPTY;
    }
}
