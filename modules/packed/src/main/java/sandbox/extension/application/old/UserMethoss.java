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

import app.packed.bean.BeanConfiguration;
import app.packed.container.Assembly;
import app.packed.container.Wirelet;

/**
 *
 */

// Taenker vi har en synthetics bean der representere applicationer.
// Det er fx den der provider services, og/eller requires dem
public interface UserMethoss {

    // or link(Ass, ApplicationWirelet.deployAsApplication());
    // or link(Ass, ApplicationWirelet.deployAsApplicationLazyBuild());

    // new LazyAssembly();
    BeanConfiguration deploy(Supplier<? extends Assembly> supplier, Wirelet... wirelets);

    void deployLazy(boolean buildLazy, Supplier<? extends Assembly> supplier, Wirelet... wirelets);
}
