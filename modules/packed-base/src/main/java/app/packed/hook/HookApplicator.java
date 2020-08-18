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
package app.packed.hook;

/**
 *
 */
// RuntimeMemberOperator
public interface HookApplicator<T> {

    // Ideen med at have target er at den skal matche typen paa component configuration...
    //// Paa den maade kan ogsaa lave mixins/sidecars. Fordi sidecars skal have en unik type...
    default Class<?> target() {
        return Class.class;
    }

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

    // Smider dem her paa extension maaske?????
    // saa man koere extension.onReady(HookApplicator, ComponentConfiguration<?> cc, Consumer<T> consumer)
    // void onReady(ComponentConfiguration<?> cc, Consumer<T> consumer);

    // Sidecar kan ikke vaere i FieldAccessor, fordi den ikke giver mening for statiske felter
    // Vi skal hellere ikke have en version kun til extension

    // Vil gerne vaek fra at vaere taet knyttet til ComponentConfiguration

//    <S> void onReady(SingletonConfiguration<?> cc, Class<S> sidecarType, BiConsumer<S, T> consumer);
}
