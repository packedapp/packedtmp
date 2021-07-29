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
package app.packed.metaannotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.function.Consumer;

//TODOs

//Must be readable by app.packed.base. Otherwise we cannot for example.. create X.many annotations

//Must test for readability on target

//All annotations must have target = Method if meta annotations is on a method
//Else fail with ComponentDefinitionException...
//Faktisk skal vi bare lave de samme tests som

//Skal have en Repeatable annotation, hvis vi har multiple  
//Supportere ikke overrides eller andre fancy ting
//Ellers fejl
/**
 *
 */
// Taenker de her ting bliver stored sammen med den almindelig annotations cache
public class MetaAnnotationResolver {

    // Returns self if no meta annotation
    static AnnotatedElement convert(AnnotatedElement element) {
        // Vi bliver noedt at loebe hver annotering igennem for at se om der er nogle
        // meta annotation... Fordi bliver noedt til at sikre os at de kan merges...

        // Maaske har vi noget a.la. // if annotations.size == 1...
        return element;
    }

    // Not an atomic operation...
    // External code must take a Lookup Object
    static void process(AnnotatedElement element, Consumer<? super Annotation> consumer) {

    }

    // AnnotatedMember -> AnnotatedMember
    // Will return self.. if no meta annotations...
}

// Kan smide de statiske metoder i AnnotationMaker