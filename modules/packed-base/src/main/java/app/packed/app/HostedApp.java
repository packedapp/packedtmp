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
package app.packed.app;

import app.packed.container.ContainerSource;
import app.packed.container.Wirelet;

/**
 * A deployed app is a special type of application that is deploy on a {@link AppHost}.
 */
// A Deployed App can have dependencies to other apps on the same host or other hosts as well????
// Maaske vi kan have noget HostLayer
// Both From and to
interface HostedApp extends App {

    void undeploy();

    // Ideen er egentligt at vi kan lave online redeployments
    void replaceWith(ContainerSource source, Wirelet... wirelets);

    // Eller ogsaa have vi en specific
    // HotDeployer newHotDeployer(String name)

    // Enten fungere den som en proxy.... Saa hosten har ingen ide om at vi skifter ud
    // Eller ogsaa er den klar over det...

}
