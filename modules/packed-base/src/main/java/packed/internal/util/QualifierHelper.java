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
package packed.internal.util;

import static packed.internal.util.StringFormatter.format;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import app.packed.base.InvalidDeclarationException;
import app.packed.base.Key;
import app.packed.base.Nullable;

/** Limited support for javax.inject classes. */
public final class QualifierHelper {

    public static void checkQualifierAnnotationPresent(Annotation e) {
        Class<?> annotationType = e.annotationType();
        // TODO check also withQualifier
        if (annotationType.isAnnotationPresent(Key.Qualifier.class)) {
            return;
        }
        // Har maaske nogle steder jeg hellere vil have IllegalArgumentException...
        // InjectExtension??? I think that's better...
        throw new InvalidDeclarationException("@" + format(annotationType) + " is not a valid qualifier. The annotation must be annotated with @Qualifier");
    }

    @Nullable
    public static Annotation[] findQualifier(AnnotatedElement element, Annotation[] annotations) {
        Annotation[] qualifiers = null;
        for (Annotation a : annotations) {
            Class<? extends Annotation> annotationType = a.annotationType();
            if (annotationType.isAnnotationPresent(Key.Qualifier.class)) {
                if (qualifiers == null) {
                    qualifiers = new Annotation[1];
                    qualifiers[0] = a;
                } else {
                    Annotation[] q = new Annotation[qualifiers.length + 1];
                    for (int i = 0; i < qualifiers.length; i++) {
                        q[i] = qualifiers[i];
                    }
                    q[qualifiers.length] = a;
                    qualifiers = q;
                }
            }
        }
        return qualifiers;
    }
}
