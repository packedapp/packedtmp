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
package app.packed.container;

import app.packed.bean.BeanExtension;
import app.packed.extension.FrameworkExtension;
import app.packed.extension.Extension.DependsOn;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionSetup;

/**
 * Ligesom bean extension bliver den kun lavet hvis der skal installeres en ny container
 */

// For
//// Container beans kunne staa med ContainerExtension som manager og de kunne
//// Information omkring containere
////// Det eneste der er lidt underligt der, er hvis ContainerExtension bliver brugt ved nye containers.
////// Saa kan container beans i root containeren jo ikke rigtig fungere

// Imod
//// Vi har ikke nogle annoteringer der skal bruges

@DependsOn(extensions = BeanExtension.class)
public class ContainerExtension extends FrameworkExtension<ContainerExtension> {

    /** The container we are installing new containers into. */
    final ContainerSetup container;

    /** Create a new container extension. */
    ContainerExtension() {
        this.container = ExtensionSetup.crack(this).container;
    }
}
