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
package app.packed.bean;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;

import app.packed.build.CodegenGenerated;
import app.packed.component.Mirror;
import app.packed.extension.Extension;
import app.packed.operation.OperationMirror;
import app.packed.util.AnnotationList;

/**
 *
 */
// Add something about why the proxy is created
// Add something about which extensions uses the proxy

// Extends Class, or interface based...

// Do we have ServiceProxy as well???
public interface BeanProxyMirror extends Mirror {

    /** {@return the bean that is proxied} */
    BeanMirror bean();

    //I think we might also have operations that are overridden
    Stream<OperationMirror> proxiedOperations();

    /** {@return the generated proxy class} */
    // Problemet med BuildGenerated er OperationTarget
    // Som vel ogsaa er en slags future. Hvis
    CodegenGenerated<Class<? super BeanProxy>> proxyClass();

    /** {@return fields that are introduced by the proxy class} */
    List<ProxyField> introducedFields();

    /** A field that is introduced on the proxy class. */
    interface ProxyField {

        /** {@return annotations on the field} */
        AnnotationList annotations();

        /** {@return the extension that introduced the field} */
        Class<? extends Extension<?>> extension();

        /** {@return the name of the field} */
        String name();

        /** {@return the type of the field} */
        Class<?> type(); // Maybe this could be generated as well??? hmmm

        CodegenGenerated<Field> field();
    }
}

// Read
// https://docs.spring.io/spring-framework/docs/4.0.x/spring-framework-reference/html/aop.html
// https://stackoverflow.com/questions/56614354/why-does-self-invocation-not-work-for-spring-proxies-e-g-with-aop

// Hmm, Maaske skal vi have en BeanClassMirror <- som er entent bean class, eller proxy class.
// Fx hvis vi introducere et interface (se nedenstaaende) paa bean klassen. Saa skal vi jo kunne se det uden at checke om vi har en proxy?
// Hmm, er det simpelthen her. Hvor vi allerede fra under scanningen kan implementere interfaces, nye metoder. Som hooks, saa kan se senere??
// Dvs indem vi laver hook scannninger, har man muligheden for at sige tilfoej denne metode, fjern denne annotering, o.s.v....
// Implement dette interface...

//Introduction: declaring additional methods or fields on behalf of a type.
// Spring AOP allows you to introduce new interfaces (and a corresponding implementation) to any advised object.
// For example, you could use an introduction to make a bean implement an IsModified interface, to simplify caching.
//(An introduction is known as an inter-type declaration in the AspectJ community.)