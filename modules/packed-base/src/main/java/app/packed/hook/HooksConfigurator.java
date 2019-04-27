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
package app.packed.hook;

import java.lang.annotation.Annotation;
import java.util.function.Consumer;
import java.util.function.Predicate;

import app.packed.bundle.WiredBundle;
import app.packed.contract.Contract;

/**
 *
 */
// Naming of capture... Is kind of an export....
public final class HooksConfigurator {

    public HooksConfigurator capture(Contract contract) {
        // Should be able to just use 1 export();
        return this;
    }

    public HooksConfigurator captureAnnotatedField(Class<? extends Annotation> hookType) {
        return this;
    }

    public HooksConfigurator captureAnnotatedMethod(Class<? extends Annotation> hookType) {
        return this;
    }

    public HooksConfigurator captureAnnotatedType(Class<? extends Annotation> hookType) {
        return this;
    }

    public HooksConfigurator captureInstanceOf(Class<?> hookType) {
        return this;
    }

    public HooksConfigurator export(Contract contract) {
        // Multiple calls to this method will be cummulative
        // We would like to able to override previous contracts, for example via annotated methods....
        return this;
    }

    public HooksConfigurator exportAnnotatedField(Class<? extends Annotation> hookType) {
        return this;
    }

    public HooksConfigurator exportAnnotatedMethod(Class<? extends Annotation> hookType) {
        return this;
    }

    public HooksConfigurator exportAnnotatedType(Class<? extends Annotation> hookType) {
        return this;
    }

    public HooksConfigurator exportInstanceOf(Class<?> hookType) {
        return this;
    }

    public <T extends Hook> HooksConfigurator filterIncoming(Class<T> hookType, Predicate<? super T> filter) {
        throw new UnsupportedOperationException();
    }

    public <T extends Hook> HooksConfigurator filterIncoming(WiredBundle wiring, Class<T> hookType, Predicate<? super T> filter) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param hookType
     *            the type of hooks to filter
     * @param filter
     *            a non-interfering, stateless predicate to apply to each hook to determine if it should be exported
     */
    public <T extends Hook> HooksConfigurator filterOutgoing(Class<T> hookType, Predicate<? super T> filter) {
        // Man kunne vaere fristet til at sige, at man burde kunne lave en der filtrer hooks udfra hvilke consumer af dem der
        // er.
        // Men det ville betyde at der ville vaere forskellige contract udfra hvilken consumer der er
        throw new UnsupportedOperationException();
    }

    // We could also explicit export it...
    public <T extends Annotation> HooksConfigurator onAnnotatedField(Class<T> annotationType, Consumer<? super AnnotatedFieldHook<T>> consumer) {
        // Lifecycle....
        throw new UnsupportedOperationException();
    }

    public HooksConfigurator onCapture() {
        // Use Listener instead???? Strictly a notification mechanism...
        return this;
    }
}
