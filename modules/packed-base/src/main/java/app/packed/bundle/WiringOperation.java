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
import java.util.ArrayList;
import java.util.List;

/**
 * In general wiring operations are thread safe stateless (take some from steam)
 */
// Wire vs link....

// Interface -> kan man let implementere, uden at fucke et nedarvnings hiraki op
// Klasse -> Vi kan have protected metoder
public abstract class WiringOperation {

    // For bedre error messages. This operation can only be used if the parent or child bundle
    // has installed the XXX extension (As an alternative, annotated the key with
    // @RequiresExtension(JMXExtension.class)....)
    // Or even better the actual WiringOperation.....
    // protected void useAttachment(Key...., Class<?> requiredExtension);

    // Bootstrap classes... Classes that are only available for injection.... (Not even initialized....)
    // bundleLink.bootstrapWith(StringArgs.of("sdsdsd");
    // bundleLink.bootstrapWith(Configuration.read("c:/sdasdasd\'");
    // run(new XBundle(), Configuration.read("c:/sdad"));

    public final WiringOperation andThen(WiringOperation nextOperation) {
        return compose(this, requireNonNull(nextOperation, "next is null"));
    }

    public final WiringOperation andThen(WiringOperation... nextOperations) {
        ArrayList<WiringOperation> l = new ArrayList<>();
        l.add(this);
        l.addAll(List.of(nextOperations));
        return compose(l.toArray(i -> new WiringOperation[i]));
    }

    // protected void validate(); Validates that the operation can be used

    protected abstract void process(BundleLink link);

    /**
     * Creates a wiring operation by composing a sequence of zero or more wiring operations.
     * 
     * @param operations
     *            the operations to combine
     * @return a new combined operation
     * @see #andThen(WiringOperation)
     * @see #andThen(WiringOperation...)
     */
    public static WiringOperation compose(WiringOperation... operations) {
        throw new UnsupportedOperationException();
    }

    static WiringOperation lookup(MethodHandles.Lookup lookup) {
        throw new UnsupportedOperationException();
    }

    static WiringOperation provide(Object o) {
        // Is this service instead???
        // Bootstrap

        // Den virker jo kun som den yderste container...
        // I den inderste skal vi
        throw new UnsupportedOperationException();
    }

}
// De bliver bare processeret ind efter den anden...
// Eller....