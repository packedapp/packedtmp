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
package app.packed.assembly;

import app.packed.container.ContainerConfiguration;

/**
 *
 */

// HMMMM
public interface AssemblyTransformer {

    /**
     * Invoked immediately before the runtime calls {@link Assembly#build()}.
     *
     * @param configuration
     *            the configuration of the container
     */
    default void beforeBuild(ContainerConfiguration configuration) {}

}
//Ideen er egentlig lidt at man inde fra en assembly. Kan faa fat i alle tilgaengelige component configurationer
//Fx giv mig alle entity bean configurations, og tilfoej dette tag...
//Maaske er det i virkeligheden bare en Stream???? Eller et TreeView

//componentConfigurations()
//<T> Stream<C componentConfigurations(Class<? extends ComponentConfiguration cl)


//Stream assignedTo();

//assembly.componentConfigurations().filterOnOperation().hasTag("foo)

interface ComponentConfigurationSet {

}
