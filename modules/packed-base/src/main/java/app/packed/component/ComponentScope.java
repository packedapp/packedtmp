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

    /** A system that contains all components in the same namespace. */
    NAMESPACE,

    /** A system that contains all components in the same application. */
    APPLICATION,

    /** A system that contains all components in the same container. */
    CONTAINER,

    /** A system that contains a single component. */
    COMPONENT,

    /**
     * A system where all components are part of the same build. Being part of the same build means that...
     */
    BUILD,

    /**
     * A system where all components are part of the same image. An image may itself contain other images.
     * <p>
     * Det her er maaske bare Application med en marker... Altsaa det hedder jo application image nu
     */
    IMAGE,

    // Den er faktisk recursive paa samme maade som vi kan have et image inde i et image
    REQUEST;

    public boolean in(Component c1, Component c2) {
        return c1.inSame(this, c2);
    }
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

    public static void main(XComp2 c) {
        c.rootOf(ComponentScope.IMAGE);
        c.isPartOfSame(ComponentScope.IMAGE, c);
        c.isPartOf(ComponentScope.IMAGE);

    }

    public static void main(Component c1, Component c2) {
        if (ComponentScope.IMAGE.in(c1, c2)) {

        }

        if (c1.inSame(ComponentScope.IMAGE, c2)) {

        }

    }
}

// Optional<> findRootOf
// rootOf(ComponentModifier.BUILD)