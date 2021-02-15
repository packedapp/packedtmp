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

import app.packed.attribute.AttributedElement;

// Hvorfor source.. Fordi vi har nogle forskellige typer
// Og fordi vi kan i nogle tilfaelde have flere sources
// F.eks. multiple Wirelets

// Alle features/input/output har vel en source????

/**
 *
 */
// Er det bare attributer???
// Det er jo rimlig forskelligt hvad vi returnere...

// Class -> Class (Obviously)
// Function -> TypeLiteral?
// Wirelet -> Class ville jeg mene
// Template File -> Path??

// Paa Component
// Set<ComponentSource> sources();

public interface ComponentSource extends AttributedElement {

    /**
     * Returns the component this source is a part of.
     * 
     * @return the component this source is a part of
     */
    Component component();

    ComponentSourceType type();
}
// Source -> Port (Port = method/field typically)
//Wirelet.$classIsPort();