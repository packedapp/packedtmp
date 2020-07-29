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
package packed.internal.component;

import packed.internal.component.PackedComponentConfigurationContext.State;

/**
 *
 */
public final class ComponentConfigurationState {

    /** The state of this configuration. */
    public State oldState = PackedComponentConfigurationContext.State.INITIAL;

    void checkNameSet() {
        switch (oldState) {
        case INITIAL:
            return;
        case FINAL:
            checkConfigurable();
        case GET_NAME_INVOKED:
            throw new IllegalStateException("Cannot call #setName(String) after the name has been initialized via calls to #getName()");
        case EXTENSION_USED:
            throw new IllegalStateException("Cannot call #setName(String) after any extensions has has been used");
        case PATH_INVOKED:
            throw new IllegalStateException("Cannot call #setName(String) after name has been initialized via calls to #path()");
        case INSTALL_INVOKED:
            throw new IllegalStateException("Cannot call this method after having installed components or used extensions");
        case LINK_INVOKED:
            throw new IllegalStateException("Cannot call this method after #link() has been invoked");
        case SET_NAME_INVOKED:
            throw new IllegalStateException("#setName(String) can only be called once");
        }
        throw new InternalError();
    }

    public final void checkConfigurable() {
        if (oldState == State.FINAL) {
            throw new IllegalStateException("This component can no longer be configured");
        }
    }
}