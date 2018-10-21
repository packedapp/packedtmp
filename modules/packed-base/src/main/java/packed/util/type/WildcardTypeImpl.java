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
package packed.util.type;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

/**
 *
 */
public final class WildcardTypeImpl implements WildcardType {

    /** The lower bound of the wildcard. */
    private Type lowerBound;

    /** The upper bound of the wildcard. */
    private Type upperBound;

    public WildcardTypeImpl(WildcardType copyOf) {
        // Type[] lowerBounds = requireNonNull(copyOf.getLowerBounds(), "copyOf.getLowerBounds() returns null");
        // Type[] upperBounds = requireNonNull(copyOf.getUpperBounds(), "copyOf.getUpperBounds() returns null");

    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /** {@inheritDoc} */
    @Override
    public Type[] getLowerBounds() {
        return new Type[] { lowerBound };
    }

    /** {@inheritDoc} */
    @Override
    public Type[] getUpperBounds() {
        return new Type[] { upperBound };
    }

    @Override
    public int hashCode() {
        return (lowerBound != null ? 31 + lowerBound.hashCode() : 1) ^ 31 + upperBound.hashCode();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
