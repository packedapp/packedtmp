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
package app.packed.extension;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import app.packed.component.ComponentConfiguration;

/**
 *
 */

// Maybe take sidecar when creating this???
//// Then we can check that the sidecar is available.. well

// RuntimeMemberOperator
public interface HookApplicator<T> {
    // Well it also works for instances

    // Kan maaske have nogle lifecycles here?????
    // Vi har jo ligesom brug sagt hvad det er vi vil have....
    // Saa descriptoren er blevet lavet...
    // Ideen er egentlig at vi kompilere
    // compile() <- maybe compile, maybe only
    // Hvis den ogsaa skal virke paa statisk saa skal den vaere paa FieldAccessor

    // Optimize???? giver det meningen at lave det per component instance??
    // Meget fine grained...
    default HookApplicator<T> optimize() {
        throw new UnsupportedOperationException();
    }

    // Den er jo ret useful fra en sidecar...
    void onReady(ComponentConfiguration cc, Consumer<T> consumer);

    // Sidecar kan ikke vaere i FieldAccessor, fordi den ikke giver mening for statiske felter
    // Vi skal hellere ikke have en version kun til extension

    <S> void onReady(ComponentConfiguration cc, Class<S> sidecarType, BiConsumer<S, T> consumer);
}
