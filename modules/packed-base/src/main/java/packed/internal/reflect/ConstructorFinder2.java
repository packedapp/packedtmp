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
package packed.internal.reflect;

import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;

import packed.internal.util.StringFormatter;
import packed.internal.util.ThrowableFactory;

/**
 *
 */
// Allow multiple constructors, For example take a list of MethodType...
// Custom ExtensionTypes

// Maybe a little customization of error messages...
// Maybe just a protected method that creates the message that can then be overridden.

// Usage
// Bundle <- No Args, with potential custom lookup object
// OnHookBuilder <- No args
// Extension <- Maybe PackedExtensionContext
// ExtensionComposer <- No Arg

// We want to create a ConstructorFinder instance that we reuse..
// So lookup object is probably an optional argument
// The rest is static, its not for injection, because we need

// So ConstructorFinder is probably a bad name..
public class ConstructorFinder2 {

    static <E extends Throwable, T> Constructor<T> findConstructor(Class<T> type, ThrowableFactory<E> tf, MethodType... types) throws E {
        // types.length == 0-> EmptyConstructor
        // Maaske tage et saet????
        // Fails if more than 1 constructor

        throw new UnsupportedOperationException();
    }

    protected String classIsAbstract(Class<?> cl) {
        return "'" + StringFormatter.format(cl) + "' cannot be an abstract class";
    }
}
