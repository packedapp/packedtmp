/*
 * Copyright (c) 2026 Kasper Nielsen.
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

import app.packed.component.ComponentHandle;
import app.packed.component.ComponentMirror;
import app.packed.component.ComponentPath;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.oldnamespace.OldNamespaceSetup;
import internal.app.packed.operation.OperationSetup;

/** Internal configuration of a component. */
public sealed interface ComponentSetup permits ApplicationSetup, ContainerSetup, BeanSetup, OperationSetup, OldNamespaceSetup {

    /** {@return the path of the component} */
    ComponentPath componentPath();

    /** {@return the external visible handle of the component} */
    ComponentHandle handle();

    /** {@return a mirror for the component} */
    ComponentMirror mirror();
}
