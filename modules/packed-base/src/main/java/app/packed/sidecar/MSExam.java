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
package app.packed.sidecar;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.base.Key.Qualifier;
import app.packed.introspection.MethodDescriptor;

/**
 *
 */

// Vi vil maaske gerne kunne styre hvordan vi injecter det sidste...
@MethodSidecar
class MSExam {

    @BootstrapSidecar
    @MyQ // Hvis vi fjerner den her kan vi maaske bare bruge denne...
    // Altsaa paa en eller anden maade skal vi ogsaa returnere en MethodFunctionDescriptor..
    // Saa vi kan se hvad vi skal hive hjem af parametere...
    static MethodHandle foo(MethodHandle mh, @MyQ String st) {
        return MethodHandles.insertArguments(mh, 3, st);
    }

    @BootstrapSidecar
    static String foo(MethodDescriptor md) {
        return md.getAnnotation(Deprecated.class).since();
    }

    @BootstrapSidecar
    @MyQ
    static String food(MethodDescriptor md) {
        return md.getAnnotation(Deprecated.class).since();
    }
}

@Qualifier
@interface MyQ {}