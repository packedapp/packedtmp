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
package app.packed.component.drivers;

import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentConfigurationContext;

/**
 * Jeg syntes ikke man som udgangspunkt bliver noedt til at dele denne klasse..
 * Hvis man har klassen kan man lave en component af den type...
 */
// Altsaa det ville vaere rart at kunne beskrive component driveren
public abstract class BindableComponentDriver<C extends ComponentConfiguration> {

    protected abstract C create(ComponentConfigurationContext context);
}

class CompDriver2 {

    public <C extends ComponentConfiguration> C bindClass(BindableComponentDriver<C> bcd, Class<?> clazz) {
        throw new UnsupportedOperationException();
    }
}