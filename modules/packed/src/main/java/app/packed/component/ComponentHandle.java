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
     * Checks that the component is still configurable or throws an {@link IllegalStateException} if not
     * <p>
     * A bean declared by the application is configurable as long as the assembly from which it was installed is
     * configurable. A bean declared by the application is configurable as long as the extension is configurable.
     *
     * @throws IllegalStateException
     *             if this handle is no longer configurable
     */
    protected final void checkIsOpen() {
        if (!isOpen()) {
            // could also go compo
            // Should probably throw InternalExtensionException
            // No because, ApplicationHandle can actually be defined by the user
            throw new IllegalStateException("The " + componentPath().componentKind().name() + " is been closed");
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
     * {@return whether or no the component can be configured by its owner}
     */
    public abstract boolean isConfigurable();

    /**
     * {@return whether or not the handle is still configurable}
     * <p>
     * A specific component instance's handle might be configurable while its configuration is not. For example, a bean that
     * is being installed will have
     */
    public abstract boolean isOpen();

    /** { @return a mirror for the component} */
    public abstract ComponentMirror mirror();
}
