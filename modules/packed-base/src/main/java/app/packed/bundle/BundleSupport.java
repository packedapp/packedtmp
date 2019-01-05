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

import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;

import packed.internal.classscan.LookupDescriptorAccessor;
import packed.internal.inject.builder.BaseBuilder;

/**
 *
 */
// BundleContext context
public final class BundleSupport {

    private final BaseBuilder[] elements;

    private BundleSupport(BaseBuilder... elements) {
        this.elements = elements;
    }

    /**
     * Returns a support class of the specified type.
     * 
     * @param type
     *            the type of support class
     * @throws UnsupportedOperationException
     *             if the specified is not supported
     */
    @SuppressWarnings("unchecked")
    public final <T> T withs(Class<? super T> type) {
        for (Object o : elements) {
            if (o.getClass() == type) {
                return (T) o;
            }
        }
        throw new UnsupportedOperationException("Does not support " + type);
    }

    /** {@inheritDoc} */
    final void lookup(Lookup lookup) {
        this.elements[0].accessor = LookupDescriptorAccessor.get(lookup);
    }

    public void configure(Bundle b) {
        requireNonNull(b);
        b.support = this;
        try {
            b.configure();
        } finally {
            b.support = null;
        }
    }

    public static BundleSupport of(BaseBuilder... elements) {
        List.of(elements); // null check
        return new BundleSupport(elements);
    }

    public enum Stage {
        DESCRIPTOR, IMAGE_GENERATION, NEW_RUNTIME;
    }
}
