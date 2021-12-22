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

import app.packed.base.Qualifier;

/**
 *
 */
// ComponentSystemType / SystemType
// ComponentViewType...
// ComponentPerspective
// Should people be allowed to make their own??? I don't think so...
// Main problem is why shouldn't people have their own modifiers then...
// Ikke til at starte med ihvertfald

// Maybe subsystem??? If we don't have one for System

// Boundary components, do we have

// A single root, and every componentde 

// This class only considers components that are in the same namespace

// Scope???
public enum ComponentScope {

    /** A scope that indicates any component in the same namespace. */
    NAMESPACE, // -> Family

    /** A scope that indicates any component within the same application. */
    CONTAINER, // -> Container

    // Der hvor denne her giver mening, er fx naar man siger install(..., spyOnWire(Scope.COMPONENT))
    // will never be inherited
    /** A scope that indicates the single component. */
    COMPONENT,

    /** A scope that indicates any component within the same application. */
    APPLICATION;

    /**
     * A system where all components are part of the same build. Being part of the same build means that...
     */
    /// BUILD

    /*
     * REALM
     *
     * Hovedgrunden til vi ikke har den med, er at extensions ikke har runtime services som boern... Saa den er lidt
     * ubrugelig
     */
    public boolean in(ComponentMirror c1, ComponentMirror c2) {
        return c1.isInSame(this, c2);
    }
}

final class Scope {
    Scope(String name) {}

    public static final Scope APPLICATION = new Scope("Application");
}

enum ComponentScope2 {

    /**
     * A system where all components are part of the same image. An image may itself contain other images.
     * <p>
     * Det her er maaske bare Application med en marker... Altsaa det hedder jo application image nu
     */
    // Tror ikke den er saa brugbar... minimum scoped as en application
    IMAGE,

    // Den er faktisk recursive paa samme maade som vi kan have et image inde i et image
    REQUEST;

}

@Qualifier
@interface Scoped {
    ComponentScope value() default ComponentScope.CONTAINER;
}

// interessant at
// roden for X
// Alle i samme X
class XComp2 {

    boolean isPartOf(ComponentScope boundaryType) {
        return false;
    }

    boolean isPartOfSame(ComponentScope boundaryType, XComp2 as) {
        return true;
    }

    /**
     * @param m
     * @return stuff
     * @throws IllegalArgumentException
     *             if the specified modifier is not supported. For example SOURCE
     */
    XComp2 rootOf(ComponentScope m) {
        return this;
    }

    // Maaske konfigurere man en event bus til det...
    public void ev(@Scoped(ComponentScope.CONTAINER) EventBus eb) {

    }

    interface EventBus {}

    public static void main(XComp2 c) {
        c.rootOf(ComponentScope.NAMESPACE);
        c.isPartOfSame(ComponentScope.NAMESPACE, c);
        c.isPartOf(ComponentScope.NAMESPACE);

    }

    public static void main(ComponentMirror c1, ComponentMirror c2) {
        if (ComponentScope.NAMESPACE.in(c1, c2)) {

        }

    }
}

// Optional<> findRootOf
// rootOf(ComponentModifier.BUILD)