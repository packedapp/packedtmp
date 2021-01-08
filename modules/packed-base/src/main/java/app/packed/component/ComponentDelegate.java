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

import app.packed.base.NamespacePath;
import app.packed.component.ComponentStream.Option;

/**
 *
 */
public interface ComponentDelegate extends ComponentSystem {

    /**
     * The component this is delegating
     * 
     * @return the component
     */
    Component component();

    /**
     * Returns a component stream consisting of this applications underlying container and all of its descendants in any
     * order.
     * <p>
     * Calling this method does <strong>not</strong> effect the lifecycle state of this application.
     * 
     * @return a component stream
     * @see #stream(Option...)
     */
    default ComponentStream stream() {
        return component().stream();
    }

    /**
     * <p>
     * This method takes a {@link CharSequence} as parameter, so it is easy to passe either a {@link String} or a
     * {@link NamespacePath}.
     * 
     * @param path
     *            the path of the component to return
     * @throws IllegalArgumentException
     *             if no component exists with the specified path
     * @return a component with the specified path
     */
    default Component resolve(CharSequence path) {
        return component().resolve(path);
    }

    /**
     * Returns a component stream consisting of all the components in this image.
     * 
     * @param options
     *            stream options
     * @return the component stream
     * @see Component#stream(app.packed.component.ComponentStream.Option...)
     */
    default ComponentStream stream(ComponentStream.Option... options) {
        return component().stream(options);
    }
}
