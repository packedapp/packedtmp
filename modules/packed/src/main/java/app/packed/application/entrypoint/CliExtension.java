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
package app.packed.application.entrypoint;

import app.packed.bean.InstanceBeanConfiguration;
import app.packed.container.Assembly;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Extension;
import app.packed.container.Wirelet;
import app.packed.inject.Factory;

/**
 *
 */
class CliExtension extends Extension<CliExtension> {

    // Skal indeholde en eller flere @CliCommands enten i selve boennen

    // Starts a new bean for the duration of the CliCommand
    // Operator = CliExtension
    <T> InstanceBeanConfiguration<T> spawn(Class<T> implementation) {
        throw new UnsupportedOperationException();
    }

    // Starts a new bean for the duration of the CliCommand
    // Operator = CliExtension
    <T> InstanceBeanConfiguration<T> spawn(Factory<T> factory) {
        throw new UnsupportedOperationException();
    }

    // Starts a new container for the duration of the CliCommand
    // Operator = CliExtension
    void spawnContainer(Assembly assembly, Wirelet... wirelets) {}

    ContainerConfiguration spawnContainer(Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }
}
