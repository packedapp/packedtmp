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
package internal.app.packed.util;

import static internal.app.packed.util.StringFormatter.format;

import java.lang.annotation.Annotation;

import app.packed.base.Nullable;
import app.packed.service.Qualifier;

/** Limited support for javax.inject classes. */
public final class QualifierUtil {

    public static void checkQualifierAnnotationPresent(Annotation e) {
        Class<?> annotationType = e.annotationType();
        // TODO check also withQualifier
        if (annotationType.isAnnotationPresent(Qualifier.class)) {
            return;
        }
        // Har maaske nogle steder jeg hellere vil have IllegalArgumentException...
        // InjectExtension??? I think that's better...
        throw new IllegalArgumentException("@" + format(annotationType) + " is not a valid qualifier. The annotation must be annotated with @Qualifier");
    }

    @Nullable
    public static Annotation[] findQualifier(Annotation[] annotations) {
        Annotation[] qualifiers = null;
        for (Annotation a : annotations) {
            Class<? extends Annotation> annotationType = a.annotationType();
            if (annotationType.isAnnotationPresent(Qualifier.class)) {
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
