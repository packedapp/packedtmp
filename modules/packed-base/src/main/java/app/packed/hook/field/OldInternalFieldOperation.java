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
package app.packed.hook.field;

import app.packed.util.Nullable;

/**
 *
 */
public class OldInternalFieldOperation {

    // What about primitives???
    Class<?> fieldType;

    // Boer vaere i fid...
    Class<?> target;

    @Nullable
    /* public final */ FunctionalInterfaceDescriptor fid;

    // Slipper sgu nok ikke udenom selv at lave dem.....(Lambdas....)
    // De behoever ikke vaere serializable.... Saa det hjaelper...

    // Kan ikke lave en method handle... eftersom vi ikke angiver et field....
    // private MethodHandle methodHandle;

    ///////// De her bliver foerst lavet med feltet....

    // public Supplier<?> newGetAccessor(ComponentConfiguration cc) {
    // MethodHandle mh = newGetter();
    // if (field().isStatic()) {
    // return new Supplier<Object>() {
    //
    // @Override
    // public Object get() {
    // try {
    // return mh.invoke();
    // } catch (Throwable e) {
    // ThrowableUtil.rethrowErrorOrRuntimeException(e);
    // throw new UndeclaredThrowableException(e);
    // }
    // }
    // };
    // } else {
    // DefaultComponentConfiguration dcc = (DefaultComponentConfiguration) cc;
    // if (dcc instanceof InstantiatedComponentConfiguration) {
    // Object instance = ((InstantiatedComponentConfiguration) dcc).getInstance();
    // return new Supplier<Object>() {
    //
    // @Override
    // public Object get() {
    // try {
    // return mh.invoke(instance);
    // } catch (Throwable e) {
    // throw new RuntimeException(e);
    // }
    // }
    // };
    // }
    // throw new UnsupportedOperationException();
    // }
    // }

    public enum OperationType {
        /** An operation that makes a single get to a field. */
        GET_ONCE, METHOD_HANDLE_GET, METHOD_HANDLE_SET, FUNCTIONAL_INTERFACE
    }

}
