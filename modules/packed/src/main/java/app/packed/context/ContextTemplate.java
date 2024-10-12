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
package app.packed.context;

import app.packed.extension.Extension;
import internal.app.packed.context.PackedContextTemplate;

/**
 *
 * <p>
 * There are no technical reasons why extensions cannot create multiple templates with different implementations.
 * However, great care must be taken to assure that the right implementations are referenced.
 * <p>
 *
 */
// Hvad er argumentent
// Hvem kan se contexten, ejeren selvfoelgelig
// Hvem kan bruge ContextValue??? Kun ejeren af context'en vil jeg mene.

// Reelt set kan vi specificere contexts for
// Operation, Bean, Container, Application

// Add template to operation.

///// VISIBILITY
// Eftersom vi har embeddable operations. Saa kan contexten jo vaere visible to many
// Visible -> Application yes|no, extensions = none, dependencies, specific extensions( det kan
// Specific extensions -> hvordan kan vi beslutte hvem??? Vi kender jo ikke extension'ene
// Og vil have det i templaten?

///// Scope
// Hvis vi laver en WebBean, saa skal contexten jo vaere med i operationen der laver bean'en
// Saa den skal jo specificeres i bean templaten og ikke paa operation handled

// Vi depender paa en context, or context impl.

public sealed interface ContextTemplate permits PackedContextTemplate {

    /** {@return the context this template is a part of.} */
    Class<? extends Context<?>> contextClass();

    /** {@return the type of value the context provides.} */
    Class<? extends Context<?>> contextImplementationClass();

    /** {@return the extension the context is a part of.} */
    Class<? extends Extension<?>> extensionClass();

    boolean isHidden();

    ContextTemplate withHidden();

    ContextTemplate withImplementation(Class<? extends Context<?>> implementationClass);

    ContextTemplate withBindAsConstant();

    static ContextTemplate of(Class<? extends Context<?>> contextClass) {
        return PackedContextTemplate.of(contextClass);
    }

    // Maaske har vi ogsaa Span her... Saa maa man bare lave mere end en instans
    // Et span er naar man tilfoejer contexten

    // never visible to extensions that does not have a dependency on the #extensionClass
    enum Visibility {
        ALL, ALL_DEPENDENCIES, HIDDEN;
    }
    // Or Private, Protected, Public
    // Ved ikke om vi kan bruge den andre steder

}
