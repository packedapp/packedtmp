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
package app.packed.bean.deprecated;

import app.packed.bean.BeanConfiguration;

/**
 *
 */

// Basically a pipeline...

// bean(instance, class, ?) -> Prepper, Prepper, Prepper, .install();

// Is also used for testing



/// ? How to we handle provide??? I think must call on returned BeanConfiguration.

// If overridden use bean locals to communicate with

// Alternativ to SyntheticBean
// Maybe I like
public class BeanPrepper<T extends BeanConfiguration> {

    public T install() {
        throw new UnsupportedOperationException();
    }

    public BeanPrepper<T> ignoreMethodsStartingWith(String name) {
        return this;
    }
}

//// UserModel
// install()-> BeanConfiguraiton
// prep() -> BeanPrepper -> BeanConfiguration

//// ExtensionModel
// BeanTemplate -> BeanInstaller -> BeanHandle

//// BuildHook

// all().prep(Consumer<BeanPrepper> prepper)



// BeanInstaller is pre-prep

// prep -> BeanTemplate -> BeanInstaller.prep
