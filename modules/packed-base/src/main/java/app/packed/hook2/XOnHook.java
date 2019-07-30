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
package app.packed.hook2;

/**
 *
 */
public @interface XOnHook {

    // Multiplicity, 0-1, 1, 0-N, 1-N, int AppMinimum, int AppMaximum.. Maaske Noget med en Context...
    // Is it per app, per bundle, ect???? ZERO_OR_ONE, ONE, ONE_OR_MORE, ANY

    // Only component with these tags
    String[] includeComponentTags() default {};

    // never components with these tags
    String[] excludeComponentTags() default {};

    String pathMatcher() default "";// "Doo.*/*"

    /**
     * @return stuff
     */
    boolean ignoreOwnContainer() default false;

    // Use cases.....

    // maybe more allowRuntimeRegistration, I think in most situations you are not going to support it????
    // failOnRuntimeRegistration, will fail if encountering an annotation for a component installed at runtime

    // Class<? extends Annotation>[] methodsAnnotatedWith() default {};

    /**
     * Will attempt to compile the resulting very aggressively. For example, via lambda factory.
     * 
     * @return stuff
     */
    // Or Class<? extends SomeOptimizer>
    boolean attemptCompilation() default false;

    //
    // Man kan registere en eller flere MethodHandleTransformation for et Hook. Maaske via annoterering
    //

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
    // Class<? extends AOPRewriter> rewriter() default AOPRewriter.class;
}
