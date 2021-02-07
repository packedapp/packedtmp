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

import java.lang.invoke.MethodHandles;

/**
 *
 */

// Saa med custom scopes saa kan man smide dem paa en realm...
// Alle componenter tilhoere en realm...

// Ja, men mht til Assembler??? Saa er det jo ikke den realm de tilhoere
// Det er istedet den lambda de laver...


// Maaske skal den hed RealmConfigurator?? RealmOrigin...
// RealmMaker
// Ahh for helvede.. Hvis der nu annotation paa Assemble??

// Maybe Realm!=Security?? Altsaa
// Hvad hvis man koere en lookup lige pludselig???
// Saa er det jo ogsaa en anden realm man skifter til??? Eller hva
// Hvad hvis lookup classes lige pludselig har nogle andre scoped annotationer??

// Altsaa hvis folk bruger Assembler... Saa er det fint de bare ser InjectorAssemble some en Realm...
// Den er kun taenkt til smaateri
// Og hvad mad ServiceComposer???
// Vil mene den bliver noedt til at vaere en konkret klasse der extender Composer...

// IDK er det to begreber vi blander sammen her???
public abstract class Realm {

    protected abstract void lookup(MethodHandles.Lookup lookup);
}
// Realm -> Composer
// Realm -> Assembly
// Realm -> Extension
// Hvad med deploy paa runtime...