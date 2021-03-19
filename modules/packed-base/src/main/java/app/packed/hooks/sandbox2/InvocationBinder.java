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
package app.packed.hooks.sandbox2;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Set;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.base.Variable;
import app.packed.inject.Factory;

/**
 *
 */
// * Resolve Strategy -> Default (Template -> MethodBuild-> ClassBuild->Service) 
//// Injection layers????s

// Maybe this is an option instead on here
// Altsaa maa fucking analysere vi bare de parametere...
// Saa de er altid automatisk templates..
// Arghhh, men nogen gange kan vi jo faa subclasses
// I think the key new thing is that unbound variables will attempted to be resolved...
// InvokerConfiguration????
public interface InvocationBinder {

    void bindArgument(int position, Factory<?> factory, int... argumentIndexes);

    void bindArgument(int position, int argumentIndex);

    void bindArgument(int position, MethodHandle transformer, int... argumentIndexes);

    /**
     * A successful invocation of this method will reduce the {@link #variableCount() variable count} with {@code 1}.
     * <p>
     * If the parameter type is a primitive, the argument object must be a wrapper, and will be unboxed to produce the
     * primitive value. (Throws NPE or CCE? on null for primitive)
     * 
     * @param position
     *            the position of the variable
     * @param constant
     *            the constant to bindW
     * @throws IndexOutOfBoundsException
     *             if the specified position is not valid
     * @throws ClassCastException
     *             if an argument does not match the corresponding variable.
     */
    void bindConstant(int position, @Nullable Object constant);

    // Must be done as the
    // Must be direct castable compatible...
    // Must be unbound
    void cast(int position, Class<?> newType);

    void cast(int position, Variable newType);

    void insert(int position, Factory<?> factory);

    // if newTypes is present, they must match every parameter
    void insert(int position, MethodHandle transformer, Variable... newTypes);

    /**
     * Returns the number of variables that bindable.
     * 
     * @return the number of variables that bindable
     */
    int variableCount();

    /**
     * Returns a list of all the variables that are bindable
     * 
     * @return a list of all the variables that are bindable
     */
    List<Variable> variables();
}

interface SandboxTemplate {

    // type must contain at least 1 method annotated with @Provide
    void provideFromArgument(Class<?> type, int argumentIndex);

}

interface SandboxReturnValues {
    // Mapning of return value to conform to Invoker
}

interface SandboxExceptions {

    // Checked exceptions thrown that are not here. Must be wrapped
    Set<Class<? extends Throwable>> uncheckedThrowables();

    void setUndeclaredException(Class<? extends Throwable> t);

}

interface SandboxOthers {
    // Ideen er at vi gerne vil have noget callback hvis en specific service
    // skal bruges. Det er en slags setup vi er ude efter...
    // Vi ved det jo ret tidligt. Og saa maaske alligevel ikke...
    // De der fucking @Foo kan vi jo ikke vide om bliver overskrevet
    // med en meta extend paa en container...
    // maaske vi bare dropper det...
    void runIfAdded(Key<?> key, Runnable action);
}

interface X {

    // Tror ikke vi viser bound parametrs.
    boolean isBound(int position);

    // Tror ikke vi vil have dem her.. Brug transform
    // Vi binder ihvertfald ikke noget
//    void bindToKey(int position, Class<?> key); // vs transform???
//
//    void bindToKey(int position, Key<?> key);

    /**
     * @param parameterIndex
     *            the index of the parameter to bind
     * @param mh
     *            a method handle whose type must match. Do we insert automatic casts??? I think it must match for the first
     *            X parameters
     */
    void parameterBindToArguments(int parameterIndex, MethodHandle mh);

    // insert instead??? bind = constants, insert = factory
    // ResolveFrom must be int -> Arguments, or Class|Key -> @Provide
    // ideen er lidt at vi indsaetter parameter fra factory i factory type
    // int argumentIndexUntil
    void parameterInsert(int index, Factory<?> factory, Object... resolveFrom);
}