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
package app.packed.classgen;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodHandles.Lookup.ClassOption;

import app.packed.extension.Extension;
import app.packed.extension.ExtensionSupport;

/**
 *
 */
//Codegen could be source code...

// Skal vi supportere noget caching?????

// Vi supportere faktisk ikke caching paa tvaers af extensions????
// Taenker vi maa definere et eller andet statisk

// ClassgenToken.of(Lookup lookup);
// CT.source()...
// Altsaa parameteren er vel en source og en slags description???

// Maaske extension ClassgenToken()

// Problemet er f.eks. naar vi koere tests...

// Vi gider jo aergelig talt ikke generere FooRepositori i hver eneste test

// Det er vel det foerste eksempel paa global state for extensions...
public final class ClassgenExtension extends Extension<ClassgenExtension> {

    // Hvis den skal virke recursivt paa nye extension.
    // Bliver vi noedt til at have en runtime classe...
    // Som kan huske svaret... Og saa konnecte via @ExtensionLinked

    // D
    // void resolveINDYsImmediatly()...

    // void disableRuntime();

    // Hvis man bruger graal er den automatiske disabled...

    public Lookup defineHiddenClass(Lookup caller, byte[] bytes, boolean initialize, ClassOption... options) throws IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    // Or event handler API... ClassDefined
    public void addListener() {}
//
//    
//    /**
//     * Provides a Classgen
//     * @return stuff
//     */
//    @Provide
//    Classgen provide() {
//        throw new UnsupportedOperationException();        //System.out.println(context.extension);
//
//    }
//    
//    interface ClassOption {}

    public final class Sub extends ExtensionSupport {

        public Lookup defineHiddenClass(Lookup caller, byte[] bytes, boolean initialize, ClassOption... options) throws IllegalAccessException {
            throw new UnsupportedOperationException();
        }

        // require runtime
        
        // require buildtime
        
//        public BeanConfiguration<Classgen> runtime() {
//            throw new UnsupportedOperationException();
//        }
//        
//        @Provide
//        Classgen provide(ProvideContext context) {
//            //System.out.println(context.extension);
//            throw new UnsupportedOperationException();
//        }
    }
}
