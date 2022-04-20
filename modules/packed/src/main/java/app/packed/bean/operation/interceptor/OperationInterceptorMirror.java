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
package app.packed.bean.operation.interceptor;

import app.packed.bean.operation.OperationMirror;
import app.packed.component.Realm;

/**
 *
 */
// GeneratedClassMirror, GeneratedBeanClassMirror
// GeneratedBeanInterceptedBeanMirror

// Man kan lave en class proxy
// Eller en bean proxy
// Eller en operation proxy (paa en enkelt operation no need to generate a proxy) 

///// Kunne ogsaa bare vaere at angive at vi maaler ting???
///// Altsaa selve Servlet pipelinen
///// Det betyder nok ogsaa at vi 
public interface OperationInterceptorMirror {

    boolean hasNext();

    boolean hasPrevious();

    OperationInterceptorMirror next();

    /** {@return the operation the interceptor is a part of.} */
    OperationMirror operation();

    // Optional<OperationInterceptorMirror> next();
    // Optional<OperationInterceptorMirror> previous();
    Realm owner();

    OperationInterceptorMirror previous();

    // Compiled into the class or wrapper in the operation pipeline

    // isPre, isPost, isAround
}

// Har vi en BeanInterceptor ogsaa????
// Eller har vi bare en InterceptorMirror
// Eller har vi en sealed

// sealed interface InterceptorMirror permits OperationInterceptorMirror, BeanInterceptorMirrors
// OperationInterceptorMirror, BeanInterceptorMirror
