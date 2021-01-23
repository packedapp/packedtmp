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
package app.packed.attribute;

/**
 * 
 */
// something that holds attributes

// AttributeContainer
// AttributeSource

// https://docs.gradle.org/current/javadoc/org/gradle/api/attributes/AttributeContainer.html

// Mht til navngivning skal vi huske at dem der implementere denne definere en masse metoder
// der helst ikke skal overskygges af metoder paa dette interface.
// Saa ikke noget med at kalde noget .lenght() eller get() osv
public interface AttributedElement {

    default <T> T attribute(Attribute<T> attribute) {
        return attributes().get(attribute);
    }

    default AttributeMap attributes() {
        throw new UnsupportedOperationException();
    }
}
