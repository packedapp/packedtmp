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
package packed.internal.hook.applicator;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.base.TypeLiteral;
import app.packed.hook.AnnotatedFieldHook;
import packed.internal.invoke.PackedIllegalAccessException;
import packed.internal.util.ThrowableUtil;

/**
 *
 */

public abstract class VarOperator<T> {

    VarOperator() {}

    /**
     * Applies this operator to the specified static field.
     * 
     * @param caller
     *            a lookup object that must have access to the specified field
     * @param field
     *            the field to operate on
     * @return the result of applying this operator
     * @throws IllegalArgumentException
     *             if the specified field is not static
     */
    public final T applyStatic(Lookup caller, Field field) {
        MethodHandle mh;
        try {
            mh = caller.unreflectGetter(field);
        } catch (IllegalAccessException e) {
            throw new PackedIllegalAccessException(e);
        }
        // Den her method handle burde jo kunne caches...

        return invoke(mh);
    }

    public final T apply(Lookup lookup, Field field, Object instance) {
        MethodHandle mh;
        try {
            mh = lookup.unreflectGetter(field);
        } catch (IllegalAccessException e) {
            throw new PackedIllegalAccessException(e);
        }
        mh = mh.bindTo(instance);
        // Den her method handle burde jo kunne caches...
        return invoke(mh);
    }

    public abstract T invoke(MethodHandle mh);

    public abstract T applyStaticHook(AnnotatedFieldHook<?> hook);

    // If its a getter we cache the method handle
    public abstract boolean isSimpleGetter();

    static class GetOnceInternalFieldOperation<T> extends VarOperator<T> {
        Class<?> fieldType;

        /** {@inheritDoc} */
        @Override
        public T invoke(MethodHandle mh) {
            try {
                return (T) mh.invoke();
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        }

        /** {@inheritDoc} */
        @Override
        public T applyStaticHook(AnnotatedFieldHook<?> hook) {
            return invoke(hook.getter());
        }

        /** {@inheritDoc} */
        @Override
        public boolean isSimpleGetter() {
            // TODO Auto-generated method stub
            return false;
        }
    }

    static class StaticSup<T> implements Supplier<T> {

        private final MethodHandle mh;

        /**
         * @param mh
         */
        public StaticSup(MethodHandle mh) {
            this.mh = requireNonNull(mh);
        }

        /** {@inheritDoc} */
        @Override
        public T get() {
            try {
                return (T) mh.invoke();
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        }

        // /** {@inheritDoc} */
        // @Override
        // public boolean isSimpleGetter() {
        // return true;
        // }
    }

    static class SupplierInternalFieldOperation<T> extends VarOperator<Supplier<T>> {
        Class<?> fieldType;

        /** {@inheritDoc} */
        @Override
        public Supplier<T> invoke(MethodHandle mh) {
            return new StaticSup<T>(mh);
        }

        /** {@inheritDoc} */
        @Override
        public boolean isSimpleGetter() {
            return true;
        }

        /** {@inheritDoc} */
        @Override
        public Supplier<T> applyStaticHook(AnnotatedFieldHook<?> hook) {
            return new StaticSup<T>(hook.getter());
        }
    }

    /**
     * @return stuff
     */
    static VarOperator<Consumer<Object>> consumer() {
        throw new UnsupportedOperationException();
    }

    static <E> VarOperator<Consumer<E>> consumer(Class<E> fieldType) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a field operator that reads a field once, and returns the value (possible null).
     * 
     * @return a field operator that reads a field once
     */
    static VarOperator<Object> getOnce() {
        return new GetOnceInternalFieldOperation<>();
    }

    /**
     * Returns a field operator that can be used to read a field exactly once.
     * 
     * @param <E>
     *            the type of field (value to get)
     * @param fieldType
     *            the type of field
     * @return the new field operator
     */
    @SuppressWarnings("unchecked")
    // TODO do we do exact check...
    // Yeah, or throws ClassCastException..
    // Den er en lille smule ligegyldig here.
    // Da brugeren formentlig vil lave et cast lige bagefter
    static <E> VarOperator<E> getOnce(Class<E> fieldType) {
        return (VarOperator<E>) getOnce();
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
    static <E> VarOperator<E> getOnce(TypeLiteral<E> fieldType) {
        return (VarOperator<E>) getOnce(fieldType.rawType());
    }

    /**
     * Returns a field operator that creates {@link MethodHandle} with getter semantics as outlined in
     * {@link Lookup#unreflectGetter(Field)}.
     * 
     * @return a field operator that will create a getter
     * @see Lookup#unreflectGetter(Field)
     */
    static VarOperator<MethodHandle> getter() {
        // Giver mening at kalde denne getter hvis AnnotatedFieldHook ogsaa skal hedde Getter
        throw new UnsupportedOperationException();
    }

    static VarOperator<MethodHandle> setter() {
        throw new UnsupportedOperationException();
    }
    // getAndSetter... is that atomic??????

    /**
     * Returns a var operator that creates a Supplier getter (Supplier).
     * 
     * @return a var operator that creates a getter.
     */
    public static VarOperator<Supplier<Object>> supplier() {
        return new SupplierInternalFieldOperation<>();
    }

    static <E> VarOperator<Supplier<E>> supplier(Class<E> fieldType) {
        return new SupplierInternalFieldOperation<>();
    }

    static <E> VarOperator<Supplier<E>> supplier(TypeLiteral<E> fieldType) {
        return new SupplierInternalFieldOperation<>();
    }

    /**
     * Returns whether or not the operator reads the field.
     * 
     * @return whether or not the operator reads the field
     */
    public boolean reads() {
        return false;
    }

    /**
     * Returns whether or not the operator writes the field.
     * 
     * @return whether or not the operator writes the field
     */
    public boolean writes() {
        return false;
    }

    VarOperator<T> validateValue(Consumer<T> validator) {
        // Idea is to be able to validate values that people set....
        // For example, > 10 ... VarOperator.consumer().validate(Validator.greatherThen(10))
        throw new UnsupportedOperationException();
    }
}
/**
 * <p>
 * Operators are typically created once and stored in a static field.
 * <p>
 * This interface is not meant to be
 */
// An operator that when applied ....
// Should be an abstract class....

// TODO Rename to VarOperator from FieldOperator....

// The requireFinal... must be gone...
// If we are called VarOperator, they better match FieldOperator
// Also readsField + writeField must be renamed...
/// **
// * Returns a new operator that will fail to work with non final-fields.
// *
// * @return the new operator
// * @see Modifier#isFinal(int)
// */
// VarOperator<T> requireFinal();
//
// default VarOperator<T> requireNonFinal() {
// return this;
// }
//
// default VarOperator<T> requireNonStatic() {
// return this;
// }

//// Ellers ogsaa checker vi dette naar vi laver en en Supplier eller lignende...
//// Move these to descriptor????
//// hook.field().checkFinal().checkAssignableTo()....
////// Nah... Tror gerne vi vil have annoteringen med...
////// Det kan vi ikke faa hvis vi har den paa descriptoren...
// AnnotatedFieldHook<T> checkAssignableTo(Class<?> type);
//
//// Move checks to field operator????
//// FieldOperator.checkFinalField().checkStatic
// AnnotatedFieldHook<T> checkExactType(Class<?> type);
//
/// **
// * Checks that the underlying field is final.
// *
// * @throws InvalidDeclarationException
// * if the underlying field is not final
// * @return this hook
// * @see Modifier#isFinal(int)
// */
// AnnotatedFieldHook<T> checkFinal();
//
/// **
// * Checks that the underlying field is not final.
// *
// * @throws InvalidDeclarationException
// * if the underlying field is final
// * @return this hook
// * @see Modifier#isFinal(int)
// */
// AnnotatedFieldHook<T> checkNotFinal();
//
/// **
// * Checks that the underlying field is not static.
// *
// * @throws InvalidDeclarationException
// * if the underlying field is static
// * @return this hook
// * @see Modifier#isStatic(int)
// */
// AnnotatedFieldHook<T> checkNotStatic();
//
/// **
// * Checks that the underlying field is static.
// *
// * @throws InvalidDeclarationException
// * if the underlying field is not static
// * @return this hook
// * @see Modifier#isStatic(int)
// */
// AnnotatedFieldHook<T> checkStatic();