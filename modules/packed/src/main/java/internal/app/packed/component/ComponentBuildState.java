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
package internal.app.packed.component;

/** The build state of a component. */
public enum ComponentBuildState {

    /** The component can be configured by its owner. */
    CONFIGURABLE_AND_OPEN,

    /**
     * The component can no longer be configured by its owner, but the extension handling the component can still configure
     * it.
     */
    OPEN_BUT_NOT_CONFIGURABLE,

    /** The component can no longer be configured by its owner or the extension handling the component. */
    CLOSED;
}
