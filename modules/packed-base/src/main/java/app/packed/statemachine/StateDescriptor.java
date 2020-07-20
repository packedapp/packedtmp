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
package app.packed.statemachine;

import java.util.Optional;
import java.util.Set;

/**
 *
 */
// Til forskel for en beskrivelse om hvilken state man er i lige nu...

// Altsaa vi bliver lige noedt til at gennemtaenke det her error...
// Normalt han man jo error states...
// Vi har mere en slags en ErrorPath... Fx. vil vi jo gerne koere @Stopping

// Eller ogsaa har en component failed state. Og vi bare koerer @Stopping alligevel???
public interface StateDescriptor {

    Optional<String> description();

    boolean isEndState();

    boolean isFailureState();

    boolean isStartState();

    String name();

    Set<String> nextStates();
}

// Hvordan haenger det samme med TaskExecution og dependencies...
// Altsaa det er vel en masse Tasks... og de har states...
// Men det er tasks der har en afhandighed og ikke states'ene...

// Altsaa man kan godt beskrive en TaskGraph... 
// Men kun hvis den er statisk. Dvs. Man ikke tilfoejer tasks undervejs...
// F.eks. Hvis X saa tilfoej den her edge, hvis Y tilfoej denne task...

// Tasks, States, Dependencies