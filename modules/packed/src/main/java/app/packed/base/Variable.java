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
package app.packed.base;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Optional;

import app.packed.hooks.sandboxinvoke.CommonVarInfo;
import app.packed.hooks.sandboxinvoke.VariableParser;
import packed.internal.bean.hooks.variable.FieldVariable;
import packed.internal.bean.hooks.variable.ParameterVariable;
import packed.internal.bean.hooks.variable.TypeVariableVariable;

/**
 * In Packed a variable (this interface) represents an annotated type of some kind This interface represents a variable of some kind, for example, a {@link Field}, Pa 
 * 
 * {@code AnnotatedVariable} represents a type and an annotated element. A variable is simple wrapper around a
 * {@link TypeToken} and an {@link AnnotatedElement}. A variable is typically constructed from a {@link Field},
 * {@link Parameter}, {@link TypeVariable} or the return type of a {@link Method}. But it can also be synthetically
 * constructed.
 */
// javadoc from AnnotatedTypeVariable

// Vis ci skal supportere capture. Bliver vi noedt til at have <T> og en abstract klasse...

// toKey(); <-- kunne bruges til fx wirelets
// asKey();
// Maybe just of(Variable v)

// Concrete class extending TypeToken????
// tror vi bliver noedt til at have specielle metoder for repeatable annotations
// Syntes vi navngiver den som .

/**
 *
 * @apiNote this interface retains the naming where possible from {@link Field}, {@link Parameter} and
 *          {@link TypeVariable}
 */
public sealed interface Variable extends AnnotatedElement permits FieldVariable, ParameterVariable, TypeVariableVariable {

    default Object getDeclaringElement() {
        // return Field
        // return Parameter
        // return Method
        // return FunctionType??? Ideen er jo lidt man kan kravle tilbage til "hele" functionen...
        // return parameterizedType
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the raw type (Class) of the variable.
     * 
     * @return the raw type of the variable
     * 
     * @see Field#getGenericType()
     * @see Parameter#getParameterizedType()
     */
    default Type getPa() {
        return getType();
    }

    
    /**
     * Returns the raw type (Class) of the variable.
     * 
     * @return the raw type of the variable
     * 
     * @see Field#getType()
     * @see Parameter#getType()
     * @see Method#getReturnType()
     * @see ParameterizedType#getRawType()
     */
    Class<?> getType();
    
    TypeToken<?> typeToken();

    // expandMetaAnnotations(); -> Declared kept. Annotations -> Meta annotations
    
    // variable().parse()
    @SuppressWarnings("exports")
    default CommonVarInfo parse() {
        return parse(CommonVarInfo.DEFAULT);
    }

    default <T> T parse(VariableParser<T> parser) {
        // extract???
        throw new UnsupportedOperationException();
    }
    
    /**
     * Returns a variable from the specified field.
     * <p>
     * 
     * @param field
     *            the field to return a variable from
     * @return a variable representing the field
     */
    static Variable ofField(Field field) {
        return new FieldVariable(field);
    }

    /**
     * Returns a variable from the return type of the specified method.
     * 
     * @param method
     *            the method to return a variable from
     * @return the variable
     */
    static Variable ofMethodReturnType(Method method) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a variable from the specified parameter.
     * 
     * @param parameter
     *            the parameter to return a variable from
     * @return the variable
     */
    static Variable ofParameter(Parameter parameter) {
        requireNonNull(parameter, "parameter is null");
        return new ParameterVariable(parameter);
    }
    
    // I think you will use FunctionType here
    public static List<Variable> ofParameters(Executable executable) {
        requireNonNull(executable, "executable is null");
        Parameter[] parameters = executable.getParameters();
        if (parameters.length == 0) {
            return List.of();
        }
        Variable[] vars = new Variable[parameters.length];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = ofParameter(parameters[i]);
        }
        return List.of(vars);
    }

    static Variable ofTypeVariable(Class<?> type, Class<?> baseType, int index) {
        return ofTypeVariables(type, baseType, index)[0];
    }

    static Variable[] ofTypeVariables(Class<?> type, Class<?> baseType, int... variables) {
        throw new UnsupportedOperationException();
    }
}

interface VariableRejected {

    // Nope via InjectionSite
    default Optional<?> source() {
        // I'm not sure that we want that
        // Parameter, Field, Method (return type), Type Variable
        return Optional.empty();
    }

}

//// Variable addNullable(); intoOptional()
//// lots of little transformations
//
///**
// * Returns the name of the variable if available.
// * <p>
// * This method should mainly be used for informational or debug purposes.
// * 
// * @return the name of the variable, or empty if the variable does not have a name
// * 
// * @see Field#getName()
// * @see Parameter#getName()
// */
//Optional<String> name();

//default Variable withName(String name) {
//  return this;
//}
//
//// How do we handle repeatable annotations?
//default Variable withAnnotation(Annotation annotation) {
//  return this;
//}
//
//default Variable withoutAnnotation(Annotation annotation) {
//  return this;
//}
//
///**
//* Returns a variable without a name
//* 
//* @return the nameless variable
//*/
//default Variable withoutName() {
//  return this;
//}
///**
// * Returns whether or not a {@link Nullable} annotation is present on the variable.
// * 
// * @return true if a nullable annotation is present, otherwise false
// */
// Hmm, now Nullable has a meaning. For example, factory.bind(null)
// would probably need to check it
//default boolean isNullable() {
//    return isAnnotationPresent(Nullable.class);
//}
// maybe just have Optional<Class<?>> source()

// TypeVariable
// Field
// Parameter
// Synthetic
// Transformed

// Syntes sgu ikke den har et navn...