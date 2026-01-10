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
package internal.app.packed.container.wirelets;

import static java.util.Objects.requireNonNull;

import app.packed.container.Wirelet;

/** A wirelet that wraps another wirelet. */
public non-sealed abstract class WrappingWirelet extends FrameworkWirelet {

    /** The wrapped wirelet. */
    protected final Wirelet wirelet;

    protected WrappingWirelet(Wirelet wirelet) {
        this.wirelet = requireNonNull(wirelet);
    }
    
    static Wirelet ignoreUnconsumed(Wirelet wirelet) {

        class IgnoreUnconsumedWirelet extends WrappingWirelet {

            /**
             * @param wirelet
             */
            protected IgnoreUnconsumedWirelet(Wirelet wirelet) {
                super(wirelet);
            }
        }
        if (wirelet instanceof IgnoreUnconsumedWirelet) {
            return wirelet;
        } else if (wirelet instanceof CompositeWirelet cw) {
            throw new UnsupportedOperationException("" + cw);
            // If composite wirelet.. Unwrap all. Call ignoreComposite. Create new Composite
        }

        // There are some issues about flags...
        // Maybe we can do something specific for wrapping wirelets
        return new IgnoreUnconsumedWirelet(wirelet);

        // Easier said then done I think. If composite wirelet.
        // We much apply to each

        // But other than that it is a kind of flag we need to carry around.
        // When apply the wirelet, not trivial. We can't just change flags
        // on the wirelet instance
        // throw new UnsupportedOperationException();
    }
}
