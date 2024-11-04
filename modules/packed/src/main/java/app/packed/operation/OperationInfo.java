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
package app.packed.operation;

import java.lang.invoke.MethodType;
import java.util.Set;

import app.packed.build.BeanSite;
import app.packed.component.ComponentPath;
import app.packed.context.Context;
import app.packed.util.AnnotationList;

/**
 * A operation site represents an operation on a bean at runtime.
 * <p>
 * The equals/hashCode contract of an operation site is based on identity. As such a single operation site will be
 * created for each operation.
 *
 * [This does not work great if we want to use at runtime... I store the site during configuration, and then compare at
 * runtime...] [Alternatively Object key() for OperationConfiguration, OperationHandle, OperationSite]
 * <p>
 * Unlike the mirror API, an operation site does not pull in information about the whole application.
 */
// Important that we only make one of them per operation.
// Because if we use it as a key in a map. The equals method is going to be very long
// Maybe we compare for Identity only... But that i shite for valhalla? I don't think we will use
// Valhalla here. It must be stored somewhere and parsed around.

// Is Static!
// Maybe we can use already with interceptors filters

// OperationInfo? I actually think that is nicer...
// The wa
// Was OperationSite
public interface OperationInfo {

    // I think by default they are on
    AnnotationList annotations();

    /** {@return the bean this operation is placed on} */
    BeanSite bean();

    /** {@return the component path of the operation} */
    // Maybe just path? IDK should we be consistant between configuration and site
    ComponentPath componentPath();

    Set<Class<? extends Context<?>>> contexts();

    // I assume since we use MHs that this does not pull in anything we don't use
    MethodType methodType();

    /** {@return the name of the operation} */
    String name();

    /**
     * {@return a set of (component) tags that have been defined for the operation}
     *
     * @see OperationConfiguration#tag(String...)
     * @see OperationConfiguration#tags()
     */
    Set<String> tags();
}

// OperationType?????? Hmmm
