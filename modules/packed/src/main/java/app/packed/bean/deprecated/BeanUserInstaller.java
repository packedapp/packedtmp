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
 * We need to able to specify shit before installing a bean
 */
// Vi undersoeger lidt muligheden for at lave 2 steps.

@Deprecated // We use BeanPreppers
public interface BeanUserInstaller<C extends BeanConfiguration> {

    C fromInstance(Class<?> clazz);

    C fromClass(Class<?> clazz);

    // install().fromClass();

    // Will be a hidden bean with a single operation taking
    // All operations on the sidecar will be managed

    BeanUserInstaller<C> named(String name);

    BeanUserInstaller<C> hidden();

    BeanUserInstaller<C> addSidecar(Class<?> clazz);
}
