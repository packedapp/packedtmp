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
package packed.internal.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;

import app.packed.util.FieldDescriptor;
import app.packed.util.InvalidDeclarationException;
import packed.internal.util.ErrorMessageBuilder;

/**
 *
 */
class Checks {

    /**
     * Checks that an annotated field is not final.
     * 
     * @param field
     *            the field to check
     * @param annotationType
     *            the type of annotation that forced the check
     */
    static void checkAnnotatedFieldIsNotFinal(FieldDescriptor field, Class<? extends Annotation> annotationType) {
        if ((Modifier.isStatic(field.getModifiers()))) {
            throw new InvalidDeclarationException("Fields annotated with @" + annotationType.getSimpleName() + " must be final, field = " + field
                    + ", to resolve remove @" + annotationType.getSimpleName() + " or make the field final");
        }
    }

    /**
     * Checks that an annotated field is not static.
     * 
     * @param field
     *            the field to check
     * @param annotationType
     *            the type of annotation that forced the check
     */
    static void checkAnnotatedFieldIsNotStatic(FieldDescriptor field, Class<? extends Annotation> annotationType) {
        if ((Modifier.isStatic(field.getModifiers()))) {
            throw new InvalidDeclarationException(
                    ErrorMessageBuilder.of(field).cannot("be static when using the @" + annotationType.getSimpleName() + " annotation")
                            .toResolve("remove @" + annotationType.getSimpleName() + " or make the field non-static"));
            //
            // throw new InvalidDeclarationException("Cannot use @" + annotationType.getSimpleName() + " on static field: " + field
            // + ", to resolve remove @"
            // + annotationType.getSimpleName() + " or make the field non-static");
        }
    }

    static String fieldCannotHaveBothAnnotations(FieldDescriptor field, Class<? extends Annotation> annotationType1,
            Class<? extends Annotation> annotationType2) {
        return "Cannot use both @" + annotationType1.getSimpleName() + " and @" + annotationType1.getSimpleName() + " on field: " + field
                + ", to resolve remove one of the annotations.";
    }
}
