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
package app.packed.component;

import static java.util.Objects.requireNonNull;

/**
 * A bundle encapsulates {@link ComponentConfiguration configuration} of a component.
 */
public abstract class Bundle<T extends ComponentConfiguration> {

    final ComponentDriver<? extends T> driver;

    protected Bundle(ComponentDriver<? extends T> driver) {
        this.driver = requireNonNull(driver);
    }
    // Maaske hedder det ikke en bundle som root???

    // Bundle, protected final
    // realm <-- protected metoder

    // Assembly, public
    // realm() <-- public metode

}

//A bundle encapsulates configuration of a component.
//Possible enhancing it with options.
//
//Captures a realm as well.
//
//Controls precisely what is exposed to users of the Bundle
//(Is typically provided to other users)
//
//Can be used exactly once.
//
//-----
//I want them to be part of the container
//and then replaced at runtime...
//
//Sounds strange that Extension is part of the container.
//But ContainerBundle is not
//
//Extensions are removed... possible replaced with an instance component
//----
//
//Always extended
//
//ActorBundle {
//
//}
