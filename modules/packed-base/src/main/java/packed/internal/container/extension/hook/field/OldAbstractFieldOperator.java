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
package packed.internal.container.extension.hook.field;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import app.packed.util.IllegalAccessRuntimeException;
import app.packed.util.TypeLiteral;
import packed.internal.container.extension.hook.PackedFieldOperator;
import packed.internal.container.extension.hook.PackedIllegalAccessException;
import packed.internal.util.StringFormatter;

/**
 *
 */
// Only return type -> A getter
// Only a single parameter -> a setter
// A return type + a single parameter -> A setter/getter
// boolean returnType + 2 identical parameters...

// some methods also take a AccessMode
// FieldAccessor<T> <T> the type of accessor

// AccessMode
// Type <- we are creating
//// Ahhh, vi vil maaske gerne have lookup object'et til at definere klassen????
//// Altsaa hvor vi skal gemme den

// Maaske skal vi kun supportere brugen af dem i forbindelse med hooks...
// Og saa lave dem per Bundle... Problemet er de gene

// usage static final FieldOperator<IntSupplier> fo = FieldOperator.custom(IntSupplier.class);

public final class OldAbstractFieldOperator<T> {

    final PackedFieldOperator<T> operation;

    private OldAbstractFieldOperator(PackedFieldOperator<T> operation) {
        this.operation = requireNonNull(operation);
    }

    public T accessInstance(MethodHandles.Lookup lookup, Field field, Object instance) {
        try {
            return accessInstance(lookup.unreflectVarHandle(field), instance);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessRuntimeException("Unable to unreflect field " + StringFormatter.format(field), e);
        }
    }

    public T accessInstance(VarHandle h, Object instance) {
        throw new UnsupportedOperationException();
    }

    public Optional<AccessMode> accessMode() {
        // Only non-null if we explicitly have specified an access mode.
        throw new UnsupportedOperationException();
    }

    public T accessStatic(MethodHandles.Lookup lookup, Field field) {
        try {
            return operation.applyStatic(lookup, field);
            // VarHandle varHandle = lookup.unreflectVarHandle(field);
            // return operation.doItStatic(varHandle, Modifier.isVolatile(field.getModifiers()));
        } catch (PackedIllegalAccessException e) {
            throw new IllegalAccessRuntimeException("Unable to unreflect field " + StringFormatter.format(field), e.getCause());
        }
    }

    public boolean isGetter() {
        // isCompareAndSet
        return false;
    }

    public boolean isSetter() {
        return false;
    }

    /**
     * Returns the type of fields this mapper can operate on. Is typically Object, but methods such as
     * {@link #supplier(Class)} defines a minimal type.
     * 
     * @return the type of fields this mapper can operate on
     */
    // Maybe rename to something a.la. field type to avoid confusion with <T>
    public Class<?> rawType() {
        // IIF the accessor puts a limit to the type of the field, for example, via
        // supplier(String.class);
        throw new UnsupportedOperationException();
    }

    public OldAbstractFieldOperator<T> withAccessMode(AccessMode accessMode) {
        throw new UnsupportedOperationException();
    }

    public static OldAbstractFieldOperator<BiPredicate<Object, Object>> compareAndSet() {
        // Do we need one with access mode???
        throw new UnsupportedOperationException();
    }

    // Must be SAM, public interface.
    public static <E> OldAbstractFieldOperator<E> sam(Class<E> type) {
        throw new UnsupportedOperationException();
    }

    public static <E> OldAbstractFieldOperator<E> custom(MethodHandles.Lookup caller, Class<E> type) {
        throw new UnsupportedOperationException();
    }

    public static <E> OldAbstractFieldOperator<E> custom(TypeLiteral<E> type) {
        // Kan aflaese parametererne fra SAM...
        //// Taenker ikke det er noget vi kan cache....
        //// TypeLiteral er bare taet paa umulig at cache...
        throw new UnsupportedOperationException();
    }

    public static OldAbstractFieldOperator<Object> getOnce() {
        return new OldAbstractFieldOperator<>(new PackedFieldOperator.GetOnceInternalFieldOperation<>());
    }

    // A single read of the field... no need to create custom classes...
    public static <E> OldAbstractFieldOperator<E> getOnce(Class<E> fieldType) {
        throw new UnsupportedOperationException();
    }

    /**
     * <p>
     * So this is basically just syntantic sugar.
     * 
     * @param <E>
     * @param fieldType
     * @return stuff
     */
    @SuppressWarnings("unchecked")
    // We could theoretically check the signature of the field....
    public static <E> OldAbstractFieldOperator<E> getOnce(TypeLiteral<E> fieldType) {
        return (OldAbstractFieldOperator<E>) getOnce(fieldType.rawType());
    }

    public static OldAbstractFieldOperator<MethodHandle> getter() {
        throw new UnsupportedOperationException();
    }

    public static OldAbstractFieldOperator<MethodHandle> setter() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a field mapper that creates a supplier
     * 
     * @return a field mapper that creates suppliers.
     */
    public static OldAbstractFieldOperator<Supplier<Object>> supplier() {
        return new OldAbstractFieldOperator<>(new PackedFieldOperator.SupplierInternalFieldOperation<>());
    }

    /**
     * @param <E>
     *            the field type
     * @param fieldType
     * @return stuff
     * @throws UnsupportedOperationException
     *             if the underlying field is an instance field and the receiving method does not support instance field
     */
    public static <E> OldAbstractFieldOperator<Supplier<E>> supplier(Class<E> fieldType) {
        // ClassValue<FieldType, ClassValue<FunctionalInterface>>
        // Can put them in a ClassValue map...
        throw new UnsupportedOperationException();
    }

    public static <E> OldAbstractFieldOperator<Supplier<E>> supplier(TypeLiteral<E> fieldType) {
        throw new UnsupportedOperationException();
    }
}
//
//// ????
// Consumer<?> newSetAccessor(ComponentConfiguration cc);
//
//// Are we going to perform type checking? other that what VarHandle does????
// <E> Consumer<? super E> newSetAccessor(ComponentConfiguration cc, Class<E> fieldType);
//
// <E> Consumer<? super E> newSetAccessor(ComponentConfiguration cc, TypeLiteral<E> fieldType);
// <A> A newAccessor(ComponentConfiguration cc, Class<A> accessorType);
//
// <A> A newAccessor(ComponentConfiguration cc, Class<A> accessorType, VarHandle.AccessMode accessMode);
//
// <A> A newAccessor(ComponentConfiguration cc, TypeLiteral<A> accessorType);
//
// <A> A newAccessor(ComponentConfiguration cc, TypeLiteral<A> accessorType, VarHandle.AccessMode accessMode);
//
// BiPredicate<?, ?> newCompareAndSetAccessor(ComponentConfiguration cc);
//
// <E> BiPredicate<E, E> newCompareAndSetAccessor(ComponentConfiguration cc, Class<E> fieldType);
//
// <E> BiPredicate<E, E> newCompareAndSetAccessor(ComponentConfiguration cc, TypeLiteral<E> fieldType);
//

// Function<?, ?> newGetAndSetAccessor(ComponentConfiguration cc);
//
// <E> Function<? super E, E> newGetAndSetAccessor(ComponentConfiguration cc, Class<E> fieldType);
//
// <E> Function<? super E, E> newGetAndSetAccessor(ComponentConfiguration cc, TypeLiteral<E> fieldType);
