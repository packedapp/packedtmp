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
package app.packed.hook.usage;

import app.packed.hook.AnnotatedComponentMethod;
import app.packed.hook.ProcessHook;
import app.packed.lifecycle.OnStart;

/**
 *
 */
public class TestIt {

    // https://en.wikipedia.org/wiki/Hooking
    @ProcessHook
    public void foo(AnnotatedComponentMethod<OnStart> method) {
        for (String s : method.annotation().after()) {
            System.out.println(s);
        }
    }

    //

    // Vi kan sgu godt lave non-static AOP... Hvis det er definere i et andet lag....
    // saa har vi f.eks. adgang til databasen,
}
// spring AOP https://www.baeldung.com/spring-aop-vs-aspectj
// Supportere determinisk bytecode generation...
// Eller paa en maade, supportere generering af bytecode i forbindelse med native image generation

// * This is proxy-based AOP, so basically you can only use method-execution joinpoints.
// * Aspects aren't applied when calling another method within the same class.
// * There can be a little runtime overhead.
// * Spring-AOP cannot add an aspect to anything that is not created by the Spring factory

// Maybe an aspect can be defined in a class of itself

// MethodProxy()... Takes