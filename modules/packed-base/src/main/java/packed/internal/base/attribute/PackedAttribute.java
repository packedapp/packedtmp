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
package packed.internal.base.attribute;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles.Lookup;

import app.packed.attribute.Attribute;
import app.packed.base.TypeToken;

/**
 *
 */
public class PackedAttribute<T> implements Attribute<T> {

    private final String displayAs;
    private final String name;

    private final Class<?> owner;

    private final Class<?> rawType;
    private final TypeToken<T> typeLiteral;

    private PackedAttribute(Class<?> owner, String name, Class<?> rawType, TypeToken<T> typeLiteral) {
        this.owner = requireNonNull(owner, "owner is null");
        this.name = requireNonNull(name, "name is null");
        this.rawType = requireNonNull(rawType, "rawType is null");
        this.displayAs = owner.getSimpleName() + "#" + name;
        this.typeLiteral = requireNonNull(typeLiteral);
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> declaredBy() {
        return owner;
    }

    /** {@inheritDoc} */
    @Override
    public String displayAs() {
        return displayAs;
    }

    /** {@inheritDoc} */
    @Override
    public String name() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> rawType() {
        return rawType;
    }

    /** {@inheritDoc} */
    @Override
    public TypeToken<T> typeLiteral() {
        return typeLiteral;
    }

    @Override
    public String toString() {
        return owner.getSimpleName() + ":" + name;
    }

    @SuppressWarnings("deprecation")
    public static <T> Attribute<T> of(Lookup lookup, String name, Class<?> rawType, TypeToken<T> type, Option<?>[] options) {
        requireNonNull(lookup, "lookup is null");
        requireNonNull(name, "name is null");
        requireNonNull(type, "type is null");
        requireNonNull(options, "options is null");
        if (!lookup.hasPrivateAccess()) {
            throw new IllegalArgumentException("The specified lookup object must have full access");
        }
        if (options.length > 0) {
            AttributeBuilder ab = new AttributeBuilder();
            for (Option<?> o : options) {
                ((PackedOption) o).process(ab);
            }
            // DoStuff

        }
        PackedAttribute<T> pa = new PackedAttribute<>(lookup.lookupClass(), name, rawType, type);
        ClassAttributes.register(pa);
        return pa;
    }

    public static <T> Attribute<T> of(Lookup lookup, String name, TypeToken<T> type, Option<?>[] options) {
        return of(lookup, name, type.rawType(), type, options);
    }

    static class AttributeBuilder {

    }

    // Ideen er at alle options extender denne
    public static abstract class PackedOption implements Attribute.Option<Object> {

        abstract void process(AttributeBuilder builder);

        public static PackedOption someSome() {
            return new PackedOption() {

                @Override
                void process(AttributeBuilder builder) {}
            };
        }
    }
}
