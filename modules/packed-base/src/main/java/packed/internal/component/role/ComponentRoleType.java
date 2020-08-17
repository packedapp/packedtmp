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
package packed.internal.component.role;

/**
 *
 */
// ComponentRole vs ComponentSpecialzation...
// Rules
// Extension always has a Container as its parent
// Guest always has a Host a parent or is root.
// An EXTENSION can never have more roles
public enum ComponentRoleType {

    // Extension giver god mening... Da vi har en masse custom lifecycle....
    // Ide'en er lidt at f.eks. Extension er en BuildInstance...
    // Omvendt taenker jeg paa at extensions kan have runtime services kun paa build time
    EXTENSION, // Maaske er det bare en Extension. En Host har en Guest...

    CONTAINER,

    HOST,

    GUEST,

    // Maaske Stateless, Statefull, Function, osv. ikke er en rolle..
    // Men en state []. One of three..

    FUNCTION,

}

//     INLINE, // Kan tilfoeje siblings paa assembly time... maaske ikke en rolle... men extensions...

// Extension | Inline + Buildtime (will not transition to instantiation)

// Extension? Hmmmm for mig virker den som om det er _en_ driver instans der altid bliver brugt... og ikke som noget man kan konfigurere
// --- Dvs den hoere eksplicit til en container... Og er maaske ikke engang public...

// MemberOfContainer?
// Single-INSTANCE
// Request
// ...

// createNewInstance+ [ManagedSingleton|ManagedMany]

//NU ER VI TILBAGE MED EN COMPONENT KAN HAVE EN ROLLE... (eller flere???)

//Role == ENUM?????

//Role = Container.class , type = DefaultContainer
//Role = Actor.class, type = SomeActor
//Role = Host.class, type = SomeHostImplementation?
//Role = Component.class, type = SomeComponent
//Role = Pooled.class, type = SomeElement.class
//Role = Singleton.class, type = ddddd
//Role = Statics.class, type = DDDDD
//Role = RunInScope.class, type = DDDDD
//Role = Unmanaged.class, type = DDDDD (Used with prototype services)
//Role = ActorSystem.class [holds actors]
//Role = Class, implementation = Class
//Role == Class only if users can define their own role....
//Role == JobManager.class
//Role == Job.class
//Role == ScheduledJob.class <- Is it a component????
