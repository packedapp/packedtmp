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
package app.packed.hook;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;

/**
 *
 */
@Retention(RUNTIME)
public @interface ProcessHook {

    Class<? extends Annotation>[] methodsAnnotatedWith() default {};

    /**
     * Will attempt to compile the resulting very aggressively. For example, via lambda factory.
     * 
     * @return
     */
    boolean attemptCompilation() default false;

    // If true AnnotatedMethodHook.methodHandle returns non-null;
    // if false AnnotatedMethodHook.methodHandle throws UnsupportedOperationException
    // boolean needsHandle default false();

    /**
     * Her er taenkt paa at vi paa en eller anden maade beskriver method transformationen. Bytekode haandteringen... Et
     * eller andet der goer at vi kan lave en generics transformation vi kan kompilere. Dette er mere eller mindre umuligt
     * 
     * 
     * @return
     */
    Class<? extends AOPRewriter> rewriter() default AOPRewriter.class;
}

class AOPRewriter {

}
// Kunne jo saadan set godt tillade, metoder der returnerede CompletableFuture....
