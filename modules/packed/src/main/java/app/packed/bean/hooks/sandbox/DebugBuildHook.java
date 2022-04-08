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
package app.packed.bean.hooks.sandbox;

/**
 *
 */
// Rename to DebugOn, DebugAtBuild

// Paa Extension...
// Paa Container  (ALT)
// Paa Bean [Class|Method|xxx]
// Paa Variable??

// Saa har vi wirelet man kan angive lidt mere globalt
// Endelig maaske en system property

// Ideen er lidt man kan smide den paa et bean, bean method, class, ...
// Og saa skriver den ting ud omkring den metode paa build time
public @interface DebugBuildHook {

    // String logger() default "System.out"
    
    // String[] retainTags 
    
    
    // int paramDependencyLevels() <-- til at debugge hvor dependencies kommer fra... i x antal levels...
    // Maaske vi skal have flere debug annoteringer... HookDebug, InjectionDebug

    boolean buildTime() default true;

    boolean runTime() default true; /// Tror ikke vi har runtime med...
    // Fx hvad vil det sige at debugge en container paa runtime???
}
