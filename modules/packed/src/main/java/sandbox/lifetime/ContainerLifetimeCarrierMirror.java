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
package sandbox.lifetime;

import java.util.Collection;

import app.packed.bean.BeanMirror;
import app.packed.container.ContainerMirror;
import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;

/**
 * Don't know if we want a separate bean mirror for this.
 */
// Do we need this mirror?
public class ContainerLifetimeCarrierMirror extends BeanMirror {

    public Class<? extends Extension<?>> extensionType() {
        return BaseExtension.class;
    }

    public Collection<ContainerMirror> containers() {
        throw new UnsupportedOperationException();
    }
}
