/*
 * Copyright (c) 2026 Kasper Nielsen.
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

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** A utility class for various functionality regarding annotations. */
public final class AnnotationUtil {

    /** Cannot instantiate. */
    private AnnotationUtil() {}

    /**
     * Validates that the specified annotation type has retention policy runtime.
     *
     * @param <T>
     *            the type of annotation to validate
     * @param annotationType
     *            the annotation type on which the retention policy should be present.
     * @return the specified annotation type
     * @throws IllegalArgumentException
     *             if a runtime retention policy was not present on the annotation type
     *
     */
    //TODO take Function<String, RuntimeException> as parameter
    public static <T extends Annotation> Class<T> validateRuntimeRetentionPolicy(Class<T> annotationType) {
        Retention r = annotationType.getAnnotation(Retention.class);
        if (r == null) {
            throw new IllegalArgumentException("The annotation type @" + annotationType.getSimpleName()
                    + " must have a runtime retention policy (@Retention(RetentionPolicy.RUNTIME), but did not have any retention policy");
        } else if (r.value() != RetentionPolicy.RUNTIME) {
            throw new IllegalArgumentException("The annotation type @" + annotationType.getSimpleName()
                    + " must have runtime retention policy (@Retention(RetentionPolicy.RUNTIME), but was " + r.value());
        }
        return annotationType;
    }

    public static boolean hasRuntimeRetentionPolicy(Class<? extends Annotation> annotationType) {
        Retention r = annotationType.getAnnotation(Retention.class);
        return r != null && r.value() == RetentionPolicy.RUNTIME;
    }
}
