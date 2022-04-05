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
package app.packed.bean.operation.examples;

import java.util.function.Predicate;

import app.packed.bean.operation.OperationMirror;

/**
 *
 */
public abstract class EventBusSubscribeMirror extends OperationMirror {

    public abstract Class<?> eventType();

    // Redelivery... Eller er det error handling???

    // Alternativt

    static Predicate<? extends EventBusSubscribeMirror> willReceiveInstance(Object o) {

        // ideen er at man kan koere
        // selectAll(SubscribeEventMirror.class).filter(SubscribeEventMirror.willReceiveInstance(x));
        // or just
        // selectAll(SubscribeEventMirror.class).filter(e->e.willReceiveInstance(x));
        throw new UnsupportedOperationException();
    }

//    // Tror nu ikke det er noget jeg ender med at lave.. Men interessant
//    interface SubscribeEventMirrorStream extends BeanOperationMirrorSelection<EventBusSubscribeMirror> {
//        default SubscribeEventMirrorStream receivesEventOfType(Class<?> x) {
//            return this;
//        }
//    }
}
