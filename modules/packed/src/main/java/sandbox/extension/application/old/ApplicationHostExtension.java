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
package sandbox.extension.application.old;

import java.util.function.Supplier;

import app.packed.assembly.Assembly;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Wirelet;
import app.packed.extension.FrameworkExtension;

/**
 *
 */
// What about
public class ApplicationHostExtension extends FrameworkExtension<ApplicationHostExtension> {

    /** Not really in the mood for guests. */
    ApplicationHostExtension() {}

    // What about concurrency, what about failures?
    // Is this when the root container is starting?
    // Or before the root container

    public void deploy(Supplier<? extends Assembly> supplier, Wirelet... wirelets) {}

    // deployment is lazy On First access?
    public void deployLazyBuild(Supplier<? extends Assembly> supplier, Wirelet... wirelets) {}

    public void deployLazyStart(Supplier<? extends Assembly> supplier, Wirelet... wirelets) {}

    // Propagation
    // ApplicationMirror

    // Virker det her???

    // Maaske har vi ikke link?
    // Og saa kun deploy??? som jo kan vaere paa BaseExtension
    public void link(Assembly assembly, Wirelet... wirelets) {}
}
class Zandbox {


    // application.requireContract();
    // Do we want to support this???
    // I think applications always has different assemblies...
    void deployOther(Supplier<? extends ContainerConfiguration> supplier, Wirelet... wirelets) {}

}