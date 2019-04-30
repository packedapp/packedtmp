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
package app.packed.bundle;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;

/**
 *
 */

public interface WiringOperation {

    // For bedre error messages. This operation can only be used if the parent or child bundle
    // has installed the XXX extension (As an alternative, annotated the key with
    // @RequiresExtension(JMXExtension.class)....)
    // Or even better the actual WiringOperation.....
    // protected void useAttachment(Key...., Class<?> requiredExtension);

    void process(BundleLink bundle);

    default WiringOperation andThen(WiringOperation next) {
        return compose(this, requireNonNull(next, "next is null"));
    }

    static WiringOperation compose(WiringOperation... operations) {
        throw new UnsupportedOperationException();
    }

    static WiringOperation lookup(MethodHandles.Lookup lookup) {
        throw new UnsupportedOperationException();
    }
}
// De bliver bare processeret ind efter den anden...
// Eller....