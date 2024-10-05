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
package app.packed.component;

import java.util.Set;

import app.packed.application.ApplicationHandle;
import app.packed.bean.BeanHandle;
import app.packed.container.ContainerHandle;
import app.packed.namespace.NamespaceHandle;
import app.packed.operation.OperationHandle;

/**
 * Tror den ryger... Maaske bliver den intern
 */
public sealed abstract class ComponentHandle permits ApplicationHandle, ContainerHandle, BeanHandle, OperationHandle, NamespaceHandle {

    /**
     * Checks that the bean is still configurable or throws an {@link IllegalStateException} if not
     * <p>
     * A bean declared by the application is configurable as long as the assembly from which it was installed is
     * configurable. A bean declared by the application is configurable as long as the extension is configurable.
     *
     * @throws IllegalStateException
     *             if the bean is no longer configurable
     */
    public final void checkIsConfigurable() {
        if (!isConfigurable()) {
            // could also go compo
            throw new IllegalStateException("The " + componentPath().componentKind().name() + " is no longer configurable");
        }
    }

    /** {@return the path of the component} */
    public abstract ComponentPath componentPath();

    /**
     * @param tags
     * @return
     */
    public abstract void componentTag(String... tags);

    public Set<String> componentTags() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns whether or not the component is still configurable.
     *
     * @return {@code true} if the component is still configurable
     */
    public abstract boolean isConfigurable();

    /** { @return a mirror for the component} */
    public abstract ComponentMirror mirror();

}
