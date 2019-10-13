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
package app.packed.service;

import java.util.function.Predicate;

import app.packed.reflect.MethodOperator;
import app.packed.reflect.VarOperator;
import app.packed.util.Key;

/**
 * 
 */

// Det tyder jo at man skal kunne lave sine egne operators..
// MethodOperator = Abstract Class, or as an alternative some factory methods....
// Maybe just some ModuleAccess
// Maybe VarOperator.Builder

// ServiceMemberOperators, hvis det er det eneste denne klasse laver....
class ServiceExtensionSupport {

    public static final VarOperator<?> INJECT_FIELD = null;

    public static final MethodOperator<?> INJECT_METHOD = null;

    public static final MethodOperator<?> injectMethod(Predicate<? super Key<?>> key) {
        throw new UnsupportedOperationException();
    }

    public static final MethodOperator<?> injectMethod(Injector injector) {
        throw new UnsupportedOperationException();
    }

    // @InjectStatic
    //// InjectStaticExtension
    //// @OnHook
}
