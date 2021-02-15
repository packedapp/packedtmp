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

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;

/**
 *
 */

// er vi ved at vaere klar til app.packed.util
// TreePath
// Maaske de descriptors...

// Eneste problem er at vi helst ikke vil loade klassen foerend til allersidst...
// Gaetter paa det bliver indy saa...
// Vi har maaske en abstract klasse, som man extender saa vi ikke behoever at
// at implmentere alle metoder...
// Problemet er primaert med Graal

// new AnnotatedElementMaker<@Foo T>(){};
public final class AnnotationMaker<T extends Annotation> {

    AnnotationMaker() {

    }

    /**
     * Returns the annotation type this maker wraps.
     * 
     * @return the annotation type this maker wraps
     */
    public Class<T> annotationType() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return an annotation instance
     * @throws UnsupportedOperationException
     *             if the annotation type has any non-default attributes
     */
    public T make() {
        throw new UnsupportedOperationException();
    }

    public T make(Object value) {
        return make("value", value);
    }

    public T make(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    public T make(String name1, Object value1, String name2, Object value2) {
        throw new UnsupportedOperationException();
    }

    public T make(String name1, Object value1, String name2, Object value2, String name3, Object value3) {
        throw new UnsupportedOperationException();
    }

    public static <T extends Annotation> AnnotationMaker<T> of(MethodHandles.Lookup lookup, Class<T> annotationType) {
        // lookup must Read Annotation
        return null;
    }
}
