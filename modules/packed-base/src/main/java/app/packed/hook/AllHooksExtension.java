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

import app.packed.container.Extension;
import app.packed.contract.Contract;

/**
 *
 */
// Naming of capture... Is kind of an export....
public final class AllHooksExtension extends Extension {

    public AllHooksExtension capture(Contract contract) {
        // Should be able to just use 1 export();
        return this;
    }

    public AllHooksExtension captureAnnotatedField(Class<? extends Annotation> hookType) {
        return this;
    }

    public AllHooksExtension captureAnnotatedMethod(Class<? extends Annotation> hookType) {
        return this;
    }

    public AllHooksExtension captureAnnotatedType(Class<? extends Annotation> hookType) {
        return this;
    }

    public AllHooksExtension captureInstanceOf(Class<?> hookType) {
        return this;
    }

    public AllHooksExtension export(Contract contract) {
        // Multiple calls to this method will be cummulative
        // We would like to able to override previous contracts, for example via annotated methods....
        return this;
    }

    public AllHooksExtension exportAnnotatedField(Class<? extends Annotation> hookType) {
        return this;
    }

    public AllHooksExtension exportAnnotatedMethod(Class<? extends Annotation> hookType) {
        return this;
    }

    public AllHooksExtension exportAnnotatedType(Class<? extends Annotation> hookType) {
        return this;
    }

    public AllHooksExtension exportInstanceOf(Class<?> hookType) {
        return this;
    }

    // We could also explicit export it...
    public <T extends Annotation> AllHooksExtension onAnnotatedField(Class<T> annotationType, Consumer<? super AnnotatedFieldHook<T>> consumer) {
        // Lifecycle....
        throw new UnsupportedOperationException();
    }

    public AllHooksExtension onCapture() {
        // Use Listener instead???? Strictly a notification mechanism...
        return this;
    }

    public class Wiring {

        public <T extends Hook> AllHooksExtension filterIncoming(Class<T> hookType, Predicate<? super T> filter) {
            throw new UnsupportedOperationException();
        }

        // alternativ er wiredBundle(HookWiring.class).doShit().... Men den er sgu svaer at finder
        // wireFilterIncoming
        // AllHooksExtension.Wiring wiring(Wiring);
        // withWiring(myNewWiring).filterStuff
        // public <T extends Hook> AllHooksExtension filterIncoming(WiredBundle wiring, Class<T> hookType, Predicate<? super T>
        // filter) {
        // throw new UnsupportedOperationException();
        // }

        /**
         * @param <T>
         *            the type of hook
         * @param hookType
         *            the type of hooks to filter
         * @param filter
         *            a non-interfering, stateless predicate to apply to each hook to determine if it should be exported
         * @return this extension
         */
        public <T extends Hook> AllHooksExtension filterOutgoing(Class<T> hookType, Predicate<? super T> filter) {
            // Man kunne vaere fristet til at sige, at man burde kunne lave en der filtrer hooks udfra hvilke consumer af dem der
            // er.
            // Men det ville betyde at der ville vaere forskellige contract udfra hvilken consumer der er
            throw new UnsupportedOperationException();
        }
    }
}
