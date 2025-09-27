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
package app.packed.bean;

import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import app.packed.bean.scanning.BeanSynthesizer;
import app.packed.util.AnnotationList;
import app.packed.util.AnnotationListTransformer;

/**
 *
 */
public interface Bean2<T> {

    default <S> Bean<T> withLocal(BeanLocal<S> local, S value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Specifies a lookup object that will be used to create invokable operations.
     *
     * @param lookup
     *            the lookup object that should be used.
     * @return the new bean
     */
    // What if we have something more complicated
    default Bean<T> withLookup(MethodHandles.Lookup lookup) {
        throw new UnsupportedOperationException();
    }

    // I want to call it open(), but we have of
    // openSuperClass
    static void registerOpeness(Class<?> clazz, MethodHandles.Lookup lookup) {
        // Ideen er her at man kan registrere super klasses
    }
}

// Hmmm, Er en Factory Op saa en Constructor????
interface Members {

    /**
     * Returns an ordered stream of all bean fields that will be scanned when the bean is installed.
     * <p>
     * The fields in the stream will be ordered in the order they will be scanned.
     *
     * @return
     */
    // Hmm a bean fields: interesting fields, or all fields?
    // If we need to remove foreign fields... They are not yet interesting
    // So probably should return all fields
    default Stream<BeanField> fields() {
        throw new UnsupportedOperationException();
    }

    // Bean.of(Foo.class).withoutFields(f->f.isAnnotatedWith(Indexed.class);
    default void withoutFields(Predicate<? super BeanField> predicate) {
        throw new UnsupportedOperationException();
    }

    default Stream<BeanField> methods() {
        throw new UnsupportedOperationException();
    }

    interface BeanMember {
        Bean<?> bean();
    }

    // Er attachments i virkeligheden et synthetisk BeanField????
    interface BeanField extends BeanMember {

    }

    interface BeanMethod extends BeanMember {}

    interface BeanConstructor extends BeanMember {}
}

interface Withers<T> {
    Bean<T> withAnnotations(AnnotationList annotations);
}

interface Transformers<T> {
    Bean<T> transformAnnotations(Consumer<? super AnnotationListTransformer> transformer);

    Bean<T> transform(Consumer<? super BeanSynthesizer> action);
}