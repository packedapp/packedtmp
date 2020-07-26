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
package sandbox.component;

import java.util.Optional;

import app.packed.container.ExtensionOrdering;

/**
 *
 */

// Bundle...

// Remove Component.extension().

//Class<? extends ComponentRealm> component.realm();

//Bundles and Extensions are build-time constructs.
//Neither is available at runtime.
//Once a subsystem transitions to initialized state. They are no longer referenced.

//But whatabout images... But they are never 

// Det er mere en slags descriptor... Men ser ikke nogen grund til at bruge

// Unless otherwise Packed will create new realms as needed...
// For example, a bundle FooBundle that defines Fff.class component
// Will have 

// Vi kan ikke gemme dem direkte i en class value... Da XBundle maaske kan
// GC's foerend component typen. Omvendt kan et barn ogsaa GC's forend en bundle

// f.eks.
// Bundle
/// setCompoentClass(Class<?> abced)'
// configure { install(abced); } <--- should not be directly stored...
// As it can be gc'ed
public interface ComponentRealm {

    // Whether or not annotations are defined that customizes the realm...
    // boolean hasCustomizations();

    // Returns all extensions the class uses...
    // Overskrives af whatever der er paa klassen...
    default ExtensionOrdering extensions(Class<?> target) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the parent realm, or empty if this realm has no parent.
     * 
     * @return the parent realm, or empty if this realm has no parent
     */
    Optional<ComponentRealm> parent();

    Class<?> realmClass();

    ComponentRealm spawn(Class<?> clazz);

    // Skal vi overhoved bruge en lookup klasse???
    // Packley Model = ComponentRealm.of(DooBundle.class).spawn(MyComp.class).modelOf(MyComp.class);

    // Tror ikke vi skal tage en look...

    static ComponentRealm of(Class<?> realmClass, Option... options) {
        return new PackedComponentRealm(realmClass);
    }

    interface Option {
        // disablePrivateAccess
        // noInheritance

        // Vi laver automatisk en child realm hvis der er behov for det...
        // F.eks. hvis der er non-public non-readable metoder..

        // disableAutocreationOfChildRealms();
    }

    public static void main(String[] args) {
        // System.out.println(MethodHandles.lookup().hasFullPrivilegeAccess());
    }
}

// Not sure it can contain anything...
// Use
// BundleDescriptor
// Or
// ExtensionDescriptor
//interface ComponentRealmDescriptor {}
