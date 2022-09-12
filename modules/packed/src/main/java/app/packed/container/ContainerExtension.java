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

import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionSetup;

/**
 *
 */

// For
//// Container beans kunne staa med ContainerExtension som owner og de kunne
//// Information omkring containere

// Imod
//// Vi har ikke nogle annoteringer der skal bruges

//// Hvis vi skal have et mirror bliver det svaert at sige det ikke skal installeres som default
//// -- Og saa giver det jo ikke mening at ApplicationExtension ikke bliver installeret ogsaa



public class ContainerExtension extends Extension<ContainerExtension> {
    
    /** The container we are installing new containers into. */
    final ContainerSetup container;

    /** Create a new container extension. */
    /* package-private */ ContainerExtension() {
        this.container = ExtensionSetup.crack(this).container;
    }

}
