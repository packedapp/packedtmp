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

/**
 *
 */

// Abstract Class????
public interface ComponentHandle {

    /**
     * Checks that the bean is still configurable or throws an {@link IllegalStateException} if not
     * <p>
     * A bean declared by the application is configurable as long as the assembly from which it was installed is
     * configurable. A bean declared by the application is configurable as long as the extension is configurable.
     *
     * @throws IllegalStateException
     *             if the bean is no longer configurable
     */
    default void checkIsConfigurable() {
        if (!isConfigurable()) {
            throw new IllegalStateException("The bean is no longer configurable");
        }
    }

    /** {@return the path of the component} */
    ComponentPath componentPath();

    /**
     * Returns whether or not the bean is still configurable.
     *
     * @return {@code true} if the bean is still configurable
     */
    boolean isConfigurable();
}
