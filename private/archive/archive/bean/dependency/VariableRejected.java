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
package archive.bean.dependency;

import java.util.Optional;

/**
 *
 */
public interface VariableRejected {

    default CommonVarInfo parse() {
        return parse(CommonVarInfo.DEFAULT);
    }

    // expandMetaAnnotations(); -> Declared kept. Annotations -> Meta annotations

    default <T> T parse(VariableParser<T> parser) {
        // extract???
        throw new UnsupportedOperationException();
    }

//  Cool with some helper method, but probably shouldn't be here
//    static Variable ofTypeVariable(Class<?> type, Class<?> baseType, int index) {
//        return ofTypeVariables(type, baseType, index)[0];
//    }
//
//    static Variable[] ofTypeVariables(Class<?> type, Class<?> baseType, int... variables) {
//        throw new UnsupportedOperationException();
//    }

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