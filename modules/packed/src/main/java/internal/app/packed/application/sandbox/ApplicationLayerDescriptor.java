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
package internal.app.packed.application.sandbox;

import java.util.Optional;

/** A descriptor of an application layer. */
public interface ApplicationLayerDescriptor {

    /** {@return the application layer class.} */
    Class<? extends ApplicationLayer> applicationLayerClass();

    /** {@return the name of the application layer.} */
    String name();

    /** {@return any parent layer of this layer.} */
    Optional<ApplicationLayerDescriptor> parent();

    /**
     * Returns a descriptor for the specified application layer class.
     * 
     * @param layerClass
     *            the type of application layer to return a descriptor for
     * @return a descriptor for the specified application layer class
     * @throws RuntimeException
     *             if the definition of the application layer is invalid.
     */
    static ApplicationLayerDescriptor of(Class<? extends ApplicationLayer> layerClass) {
        throw new UnsupportedOperationException();
    }
}
