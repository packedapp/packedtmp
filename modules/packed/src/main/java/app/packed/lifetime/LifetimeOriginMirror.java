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
package app.packed.lifetime;

import app.packed.base.NamespacePath;
import app.packed.bean.BeanMirror;
import app.packed.container.ContainerMirror;

/**
 * A mirror of a component.
 * <p>
 * Instances of this is interface is always either a {@link ContainerMirror} or {@link BeanMirror} instance.
 * <p>
 * A component is the basic entity in Packed. Much like everything is a is one of the defining features of Unix, and its
 * derivatives. In packed everything is a component.
 */
public sealed interface LifetimeOriginMirror permits ContainerMirror, BeanMirror {

    /**
     * Returns the name of this component.
     * <p>
     * If no name was explicitly set when the component was configured. Packed will automatically assign a name that is
     * unique among other components with the same parent.
     *
     * @return the name of this component
     */
    String name();

    /** {@return the path of this component} */
    NamespacePath path();
}
