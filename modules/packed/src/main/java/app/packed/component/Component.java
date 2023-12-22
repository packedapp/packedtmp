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
 * A component is generic xxx
 * <p>
 * All components have a unique component path that disguished from any other component with a single application (deployment).
 */

// Maybe BeanHandle, BeanMirror, BeanConfiguration all extend this interface
// I'm not sure it is super useful.. And maybe more con
public interface Component {

    /** {@return the path of the component} */
    ComponentPath componentPath();
}
// Owner does not make on a component. Who owns an extension (if it is a component).
// Who owns a namespace?let us say two extensions shares one.