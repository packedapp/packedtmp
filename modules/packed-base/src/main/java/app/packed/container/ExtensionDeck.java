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
package app.packed.container;

/**
 *
 */

// 3 users

// Den extension der laver den
// Folk der bruger den extension
// Extensions der bruger Subtension

public interface ExtensionDeck<T> {

    ExtensionList extensions(); // Any extensions that have been used

    // I think it should fail if the extension has not been registered
    void addBefore(Class<? extends Extension> extensionType);
}

interface ExtensionCon {

    <T> ExtensionDeck<T> newDecoratedChain(Class<T> type);
}