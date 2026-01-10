/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package app.packed.build.hook.cleanup;

import java.util.List;

import app.packed.build.hook.BuildHookMirror;
import app.packed.component.ComponentMirror;

/**
 *
 */
// Forskellen er at der kan eksistere flere af dem her per DeclarationMirror
// Hvis vi har 21 beans. Saa har vi 21 af dem her
// Det der er, er at vi gerne vil kunne finde ud af hvordan helvede et Hook er kommet paa den her bean.
// Alternativt. Optional<?> DeclartionMirror.resolve(ComponentPath component);


// TROR VI SMIDER DEN UD og putter ting direkte paa componenterne
// Eller BeanMirror
// List<BuildHook> buildHooks();
// Map<Class<? extends BuildHook> List<BuildHook>> appliedBuildHooks(); // Includes Component mirrors (and for beans include stuff defined on assembly

// OperationMirror
//List<BuildHook> buildHooks();

public interface AppliedBuildHookMirror {

    BuildHookMirror buildHook();

    /** {@return the component where the build hook is applied} */
    ComponentMirror component();

    /**
     * {@return a list of all applied build hook applied for the #component()}
     */
    List<AppliedBuildHookMirror> list();

    /**
     * {@return the index of this applied hook into #list()}
     */
    int listIndex();
}
