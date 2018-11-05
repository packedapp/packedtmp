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
package packed.internal.inject;

import static packed.internal.util.StringFormatter.format;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import app.packed.inject.Inject;
import app.packed.inject.Provider;
import app.packed.inject.Qualifier;
import app.packed.util.InvalidDeclarationException;

/** Limited support for javax.inject classes. */
@SuppressWarnings("unchecked")
public final class JavaXInjectSupport {

    /** The available qualifier annotations. */
    private static final Class<? extends Annotation>[] INJECT_ANNOTATIONS = JavaxInjectSupport.isPresent()
            ? new Class[] { Inject.class, JavaxInjectSupport.INJECT }
            : new Class[] { Inject.class };

    /** The available qualifier annotations. */
    static final Class<?>[] PROVIDER_INTERFACES = JavaxInjectSupport.isPresent() ? new Class[] { Provider.class, JavaxInjectSupport.PROVIDER }
            : new Class[] { Provider.class };

    /** The available qualifier annotations. */
    static final Class<? extends Annotation>[] QUALIFIER_ANNOTATIONS = JavaxInjectSupport.isPresent()
            ? new Class[] { Qualifier.class, JavaxInjectSupport.QUALIFIER }
            : new Class[] { Qualifier.class };

    public static boolean checkQualifierAnnotationPresent(AnnotatedElement e) {
        for (Class<? extends Annotation> a : JavaXInjectSupport.QUALIFIER_ANNOTATIONS) {
            if (e.isAnnotationPresent(a)) {
                throw new IllegalArgumentException("@" + a.getSimpleName() + " is not a valid qualifier. The annotation must be annotated with @Qualifier");
            }
        }
        return false;
    }

    public static void checkQualifierAnnotationPresent(Annotation e) {
        Class<?> annotationType = e.annotationType();
        for (Class<? extends Annotation> a : JavaXInjectSupport.QUALIFIER_ANNOTATIONS) {
            if (annotationType.isAnnotationPresent(a)) {
                return;
            }
        }
        throw new InvalidDeclarationException("@" + format(annotationType) + " is not a valid qualifier. The annotation must be annotated with @Qualifier");
    }

    public static boolean isInjectAnnotationPresent(AnnotatedElement e) {
        for (Class<? extends Annotation> a : JavaXInjectSupport.INJECT_ANNOTATIONS) {
            if (e.isAnnotationPresent(a)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOptionalType(Class<?> type) {
        return type == Optional.class || type == OptionalLong.class || type == OptionalInt.class || type == OptionalDouble.class;
    }

    public static boolean isQualifierAnnotationPresent(AnnotatedElement e) {
        for (Class<? extends Annotation> a : JavaXInjectSupport.QUALIFIER_ANNOTATIONS) {
            if (e.isAnnotationPresent(a)) {
                return true;
            }
        }
        return false;
    }

    public static List<AnnotatedElement> getAllQualifierAnnotationPresent(AnnotatedElement e) {
        ArrayList<AnnotatedElement> result = new ArrayList<>();
        if (isQualifierAnnotationPresent(e)) {
            result.add(e);
        }

        return result;
    }

    /**
     * A class that attempts to load the javax.inject class.
     */
    static class JavaxInjectSupport {

        /** The javax.inject.Inject class. */
        static final Class<? extends Annotation> INJECT;

        /** The javax.inject.Provider class. */
        static final Class<?> PROVIDER;

        /** The javax.inject.Qualifier class. */
        static final Class<? extends Annotation> QUALIFIER;

        static {
            Class<?> qualifier = null;
            Class<?> provider = null;
            Class<?> inject = null;
            try {
                qualifier = Class.forName("javax.inject.Qualifier");
                provider = Class.forName("javax.inject.Provider");
                inject = Class.forName("javax.inject.Inject");
            } catch (ClassNotFoundException ignore) {}
            QUALIFIER = (Class<? extends Annotation>) qualifier;
            PROVIDER = provider;
            INJECT = (Class<? extends Annotation>) inject;
        }

        static boolean isPresent() {
            return QUALIFIER != null;
        }
    }
}
