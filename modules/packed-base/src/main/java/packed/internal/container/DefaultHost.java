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
package packed.internal.container;

import java.util.concurrent.ConcurrentHashMap;

import app.packed.app.App;

/**
 *
 */
public class DefaultHost extends AbstractComponent implements ComponentHolder {

    // App is not a component, so can't really use children. Unless, we attach the artifact
    // to the component, which we probably should
    final ConcurrentHashMap<String, App> apps = new ConcurrentHashMap<>();

    /**
     * @param configuration
     */
    DefaultHost(AbstractComponentConfiguration configuration, PackedArtifactInstantiationContext ic) {
        super(null, configuration, ic);
    }
}
