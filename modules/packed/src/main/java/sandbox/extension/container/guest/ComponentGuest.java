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
package sandbox.extension.container.guest;

import java.lang.invoke.MethodHandle;
import java.util.Optional;
import java.util.Set;

import app.packed.component.ComponentMirror;

/**
 *
 */
// Vi proever at klare alle 3 here. Så er det let som bruger at kunne starte en container/application/bean hvis man supportere alle 3
public interface ComponentGuest {
    /**
     * {@return the name of the guest}
     */
    String name(); // I think just name of the bean??? For image rep-> GeneratedName

    Set<String> tags();

    ComponentMirror mirror(); // Maybe throw UOE, Mirrors are only available if configured

    MethodHandle launcher();

    boolean isManaged();

    boolean hasDestructor();

    boolean isPermanent(); // Can be undeployed

    Optional<MethodHandle> destructor(); // For beans that does not have wrappers

    enum ComponentGuestKind {
        APPLICATION, CONTAINER, BEAN;
    }

}
