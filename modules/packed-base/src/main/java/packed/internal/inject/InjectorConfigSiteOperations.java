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
package packed.internal.inject;

/**
 *
 */

// Separate for bundle/configuration?????
public final class InjectorConfigSiteOperations {

    /** */
    public static final String EXPORT_SERVICE = "Injector.expose";

    /** */
    public static final String INJECTOR_CONFIGURATION_BIND = "Injector.bind";

    public static final String INJECTOR_CONFIGURATION_ADD_DEPENDENCY = "Injector.addDependency";

    /** */
    public static final String INJECTOR_CONFIGURATION_INJECTOR_BIND = "Injector.injectorBind";

    /** */
    public static final String INJECTOR_OF = "Injector.of";

    /** */
    public static final String INJECTOR_PROVIDE = "Injector.provide";

    public static final String COMPONENT_INSTALL = "Component.install";

}
