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
package app.packed.namespace;

import app.packed.component.ComponentHandle;

/**
 * Used by the extension
 */
// Skal vi baade have Handle og Operator???
// Operatoren er jo god. Fordi man i 9/10 tilfaelde vil gemme noget information omkring namespaced...
// Og fordi jeg ogsaa tror vi faar nogle callbacks...

// Og hvis ikke skal vi ikke saa kun have Operator ogsaa for beans, osv
public interface NamespaceHandle extends ComponentHandle {

    String name();

//    /**
//     * @param name
//     */
//    void named(String name);

}
