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

import java.util.function.Consumer;

import app.packed.bean.InstanceBeanConfiguration;
import app.packed.bean.scanning.BeanSynthesizer;

/**
 * We need to able to specify shit before installing a bean
 */
// Vi undersoeger lidt muligheden for at lave 2 steps.

@Deprecated // We use BeanPreppers
public interface BeanUserInstaller2 {

    // Sidecar laver x instanser af en sidecar der bliver lavet sammen med beanen
    // Alle metoder bliver flyttet fra den ene bean til den anden.
    // Paa naer constructuren paa sidecar'en. Den bliver maaske lavet til en alm void operation
    // paa parent beanen sideConstruct-> returne sidecarinstance
    // Saa ser alt ud som om det er den oprindelige bean

    // install().fromClass();

    // Will be a hidden bean with a single operation taking
    // All operations on the sidecar will be managed

    BeanUserInstaller2 transformBean(Consumer<? super BeanSynthesizer> transformer);

    // Eneste problem er at vi nu ikke ved hvad det er for en type beans vi manager..
    // Saa for at laese en bean skal vi have en extractor (Possible NO) da kan beanen ud af

    // Hvem har brug for selve bean instancen???
    // Maaske kan vi have en proxy

    //// I think we probably need some way to specify @Host Let us say the parent bean implments a generic interface
    BeanUserInstaller2 installSidecar(Class<?> sidecar);

    <T> BeanUserInstaller2 installSidecar(Class<T> sidecar, Consumer<? super InstanceBeanConfiguration<? super T>> configurator);

    //

//    BeanUserInstaller2<C> hidden();
}
