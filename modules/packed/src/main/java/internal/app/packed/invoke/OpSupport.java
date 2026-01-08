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
package internal.app.packed.invoke;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;

/** Support methods for handling {@link app.packed.operation.Op}. */
public final class OpSupport {

    /** A method handle for {@link #accept(Consumer, Object)}. */
    public static final MethodHandle ACCEPT_CONSUMER_OBJECT = LookupUtil.findStaticSelf(MethodHandles.lookup(), "acceptConsumerOp", Object.class, Consumer.class, Object.class);

    @SuppressWarnings({ "unused", "unchecked", "rawtypes" })
    private static Object acceptConsumerOp(Consumer consumer, Object object) {
        consumer.accept(object);
        return object;
    }
}
