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
package app.packed.hooks.sandbox2;

import app.packed.hooks.ClassHook;
import app.packed.inject.Factory;

/**
 *
 */

// Extension Runtime ->

// Share
public @interface MHook {

    abstract class ActualBootStrap {
        
        // @Foo cannot be specified at runtime component registration
        // Ellers taenker jeg at putte det paa MethodHook annoteringer...
        
        // Hmm hvis vi nu bruger managed ClassHooks... Hvordan skal de saa forholde sig?
        // De er maaske ikke runtime registreret...
        protected static final void $AllowRuntimeRegistration(Class<?> supportClass) {
            
        }
    }
    
    interface Bootstrap {
        
        Class<?> buildType();
        void buildWith(Object instance);
        void buildWithPrototype(Class<?> implementation);
        void buildWithProtoype(Factory<?> factory);
        void buildDisable();

        <T extends ClassHook.Bootstrap> T manageWithClassHook(Class<T> classBootstrap);

        Class<?> runType();
        void runWith(Object instance);
        void runDisable(); //?? vs BuildDisable
        void runWithPrototype(Class<?> implementation);
        void runWithProtoype(Factory<?> factory);
    }


    // Build Inject 
    // Bootstrap class + ManagedByBootstrap<---nah behoever vi + ManagedByBuild
    // Tror vi dropper ManagedByBootstrap.. Hvis det skal deles maa det med over i ManagedByBuild...

    // Saa Bootstrap class + ManagedByBuild (Single class-value if managed by...) + Extension
    interface BuildContext {
        
        boolean hasExtension(); //true if ExtensionMember and not a runtime loose cannon registration
        
        Class<? extends Bootstrap> bootstrap();// What about classHooks.
        
        Class<?> runType();
        void runWith(Object instance);
        void runDisable();
        void runWithPrototype(Class<?> implementation);
        void runWithProtoype(Factory<?> factory);
    }

    // InjectionContext paa @OnStart kan ikke calculeres foerend de bliver kaldt...
    // Men samtidig vil vi gerne have verifieret at f.eks. @OnStop ikke bruger en invalid service....
    
    interface RunContext {

    }
}
// Ville vaere rigtig go

// Attributes
