/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package app.packed.build.hook;

/**
 * <p>
 * Propagation deals with nodes in a tree...
 * It is kind of TreeTraversel
 *
 *
 */
// Hmm, det her lidt en placeholder fordi jeg ikke helt ved hvad jeg skal goere


// Propagation er releveant for BuildObservers og BuildTransformers
// Og vel også på noget Namespace/Scope.. fx can et service namespace jo sagtens deles over flere assemblies

// Typisk Assembly/Container

// Men saa er der jo egentlig ogsaa filtre..
// Maaske er proapgation altid kun assemblies, og saa er filtre

// Maske er det det, at det er et trae der er forskellen???
// En bean er ikke et trae
// Det er en slags apply, goDown.
// Saa det er filter plus om man vil gaa videre med en boernenode


// Kunne ogsaa returneres direkte fra transformersne???
// Dvs ContainerTransformer, ApplicationTransformer....
// Hmm, virker ikke super godt
public interface BuildPropagation {

    // Maaske er det Assembly og Container i en?
    // Maaske er det kun Assembly... Og saa maa man filtrer direkte i build hooket. Tror det goer ting mere simple

}
