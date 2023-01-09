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
package internal.app.packed.service;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.OptionalInt;

import app.packed.service.Key;
import app.packed.service.GenericType;
import internal.app.packed.util.QualifierUtil;

/**
 *
 */
public class KeyHelper {

    /**
     * Returns a key matching the type of the specified field and any qualifier that may be present on the field.
     * 
     * @param field
     *            the field to return a key for
     * @return a key matching the type of the field and any qualifier that may be present on the field
     * @throws RuntimeException
     *             if the field does not represent a valid key. For example, if the type is an optional type such as
     *             {@link Optional} or {@link OptionalInt}. Or if there are more than 1 qualifier present on the field
     * @see Field#getType()
     * @see Field#getGenericType()
     */
    // I think throw IAE. And then have package private methods that take a ThrowableFactory.
    // RuntimeException -> should be ConversionException
    
    // TODO move to introspector, And then we can throw BeanDE
    // Or at least have their own version
    public static Key<?> convertField(Field field) {
        GenericType<?> tl = GenericType.fromField(field).wrap(); // checks null
        Annotation[] annotation = QualifierUtil.findQualifier(field.getAnnotations());
        return Key.convertTypeLiteralNullableAnnotation(field, tl, annotation);
    }

    /**
     * Returns a key matching the return type of the specified method and any qualifier that may be present on the method.
     * 
     * @param method
     *            the method for to return a key for
     * @return the key matching the return type of the method and any qualifier that may be present on the method
     * @throws RuntimeException
     *             if the specified method has a void return type. Or returns an optional type such as {@link Optional} or
     *             {@link OptionalInt}. Or if there are more than 1 qualifier present on the method
     * @see Method#getReturnType()
     * @see Method#getGenericReturnType()
     */
    public static Key<?> convertMethodReturnType(Method method) {
        requireNonNull(method, "method is null");
        // Maaske er det langsommere med AT end de andre
        AnnotatedType at = method.getAnnotatedReturnType();
        if (at.getType() == void.class) {
            throw new RuntimeException("@Provides method " + method + " cannot have void return type");
        }
        GenericType<?> tl = GenericType.fromMethodReturnType(method).wrap();
        Annotation[] annotation = QualifierUtil.findQualifier(at.getAnnotations());
        return Key.convertTypeLiteralNullableAnnotation(method, tl, annotation);
    }
}
