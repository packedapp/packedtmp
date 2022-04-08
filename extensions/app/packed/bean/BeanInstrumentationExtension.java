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
package app.packed.bean;

import app.packed.classgen.ClassgenExtension;
import app.packed.extension.Extension;
import app.packed.extension.Extension.DependsOn;
import app.packed.inject.Factory;

/**
 * Ideen er at man kan lave en proxy bean ved at extende en eksisterende klasse. FX JPA
 */
@DependsOn(extensions = { ClassgenExtension.class, BeanExtension.class })
public class BeanInstrumentationExtension extends Extension<BeanInstrumentationExtension> {
    BeanInstrumentationExtension() {}

    // Problemet er her provide()
    // Vi skal maaske have en provide(ApplicationBeanConfiguration)

    // Installs what looks like an ApplicationBean. But is lazily initialized and started
    
    // Maaske er der operation lazy. Og saa Bean Lazy.
    //// Operation lazy behoever vi ikke at instrumentere klassen.
    public <T> ContainerBeanConfiguration<T> installLazy(Class<T> clazz) {
        throw new UnsupportedOperationException();
    }

    public <T> ContainerBeanConfiguration<T> installLazy(Factory<T> clazz) {
        throw new UnsupportedOperationException();
    }

    public void interceptAll() {}
}
