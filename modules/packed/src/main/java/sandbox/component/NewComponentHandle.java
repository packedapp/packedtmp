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
package sandbox.component;

import java.util.Optional;

import app.packed.component.ComponentPath;
import app.packed.extension.Extension;

/**
 *
 */
// 1. Ideen er at proeve at slippe for XHandle.Builder
// 2. Ideen er at lade extensions kunne definere deres egne component handlere
// 3. Ideen er at supportere BuildPermissions paa den her på en eller anden måde

// I think we some meta shit here.. ComponentMetaTemplate
public abstract class NewComponentHandle<E extends Extension<?>> {

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
            throw new IllegalStateException("The bean is no longer configurable");
        }
    }

    // Eller er der ogsaa noget scope ting???
    public final Optional<ComponentPath> authorityComponent() {
        throw new UnsupportedOperationException(); // vs parentComponent (Only for paths)
    }

    public final void checkInPreppingState() {

    }

    /**
     * {@return the path of the component}
     *
     * @throws IllegalStateException
     *             if the component is in the preparing state and has not yet been installed
     */
    public final ComponentPath componentPath() {
        checkInPreppingState();
        throw new UnsupportedOperationException();
    }

    protected void install(Object o) {
        // Maybe have an abstract Handle
    }

    /**
     * Returns whether or not the bean is still configurable.
     *
     * @return {@code true} if the bean is still configurable
     */
    public final boolean isConfigurable() {
        throw new UnsupportedOperationException();
    }

    public final boolean isInstalled() {
        throw new UnsupportedOperationException();
    }

    protected final void nameComponent(String name) {
        // usage
        // MyHandle named(String name) {nameComponent(name); return this};
    }

    /** {@return the current state of this handle} */
    public final NewComponentHandle.State state() {
        throw new UnsupportedOperationException();
    }

    public enum State {
        PREPARING, INSTALLED, CONFIGURED;
    }
}
