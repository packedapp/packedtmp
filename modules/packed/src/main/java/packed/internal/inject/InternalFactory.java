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
package packed.internal.inject;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.base.TypeToken;
import app.packed.inject.CapturingFactory;
import app.packed.inject.Factory;
import packed.internal.bean.inject.InternalDependency;
import packed.internal.util.LookupUtil;
import packed.internal.util.MethodHandleUtil;

/**
 *
 */
public abstract non-sealed class InternalFactory<R> extends Factory<R> {

    /** The type of objects this factory creates. */
    private final TypeToken<R> typeLiteral;

    public InternalFactory(TypeToken<R> typeLiteralOrKey) {
        requireNonNull(typeLiteralOrKey, "typeLiteralOrKey is null");
        this.typeLiteral = typeLiteralOrKey;
    }

    public abstract MethodHandle toMethodHandle(Lookup lookup);

    public abstract List<InternalDependency> dependencies();

    /** A var handle that can update the {@link #configuration()} field in this class. */
    private static final VarHandle VH_CF_DEPENDENCIES = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), CapturingFactory.class, "dependencies", List.class);

    /** A var handle that can update the {@link #configuration()} field in this class. */
    private static final VarHandle VH_CF_METHOD_HANDLE = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), CapturingFactory.class, "methodHandle", MethodHandle.class);

    public static final List<InternalDependency> dependencies(Factory<?> factory) {
        if (factory instanceof InternalFactory<?> f) {
            return f.dependencies();
        } else {
            return (List<InternalDependency>) VH_CF_DEPENDENCIES.get(factory);
        }
    }

    public static final MethodHandle toMethodHandle0(Factory<?> factory, Lookup lookup) {
        if (factory instanceof InternalFactory<?> f) {
            return f.toMethodHandle(lookup);
        } else {
            return (MethodHandle) VH_CF_METHOD_HANDLE.get(factory);
        }
    }

    /** {@inheritDoc} */
    @Override
    public TypeToken<R> typeLiteral() {
        return typeLiteral;
    }

    /** A special factory created via {@link #withLookup(Lookup)}. */
    // A simple version of Binding... Maybe just only have one
    public static final class BoundFactory<T> extends InternalFactory<T> {

        private final Object[] arguments;

        /** The ExecutableFactor or FieldFactory to delegate to. */
        private final Factory<T> delegate;

        private final List<InternalDependency> dependencies;

        /** The ExecutableFactor or FieldFactory to delegate to. */
        private final int index;

        public BoundFactory(Factory<T> delegate, int index, InternalDependency[] dd, Object[] arguments) {
            super(delegate.typeLiteral());
            this.index = index;
            this.delegate = requireNonNull(delegate);
            this.arguments = arguments;
            this.dependencies = List.of(dd);
        }

        /** {@inheritDoc} */
        @Override
        public List<InternalDependency> dependencies() {
            return dependencies;
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle toMethodHandle(Lookup lookup) {
            MethodHandle mh = toMethodHandle0(delegate, lookup);
            return MethodHandles.insertArguments(mh, index, arguments);
        }
    }

    /** A factory that provides the same value every time, used by {@link Factory#ofConstant(Object)}. */
    public static final class ConstantFactory<T> extends InternalFactory<T> {

        /** The value that is returned every time. */
        private final T instance;

        @SuppressWarnings("unchecked")
        public ConstantFactory(T instance) {
            super((TypeToken<T>) TypeToken.of(instance.getClass()));
            this.instance = instance;
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle toMethodHandle(Lookup ignore) {
            return MethodHandles.constant(instance.getClass(), instance);
        }

        /** {@inheritDoc} */
        @Override
        public List<InternalDependency> dependencies() {
            return List.of();
        }
    }

    /** A special factory created via {@link #withLookup(Lookup)}. */
    public static final class LookedUpFactory<T> extends InternalFactory<T> {

        /** The ExecutableFactor or FieldFactory to delegate to. */
        private final Factory<T> delegate;

        /** The method handle that was unreflected. */
        private final MethodHandle methodHandle;

        public LookedUpFactory(Factory<T> delegate, MethodHandle methodHandle) {
            super(delegate.typeLiteral());
            this.delegate = delegate;
            this.methodHandle = requireNonNull(methodHandle);
        }

        /** {@inheritDoc} */
        @Override
        public List<InternalDependency> dependencies() {
            return dependencies(delegate);
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle toMethodHandle(Lookup ignore) {
            return methodHandle;
        }
    }

    /** A factory for {@link #peek(Consumer)}}. */
    public static final class PeekableFactory<T> extends InternalFactory<T> {

        /** A method handle for {@link Function#apply(Object)}. */
        private static final MethodHandle ACCEPT = LookupUtil.lookupStatic(MethodHandles.lookup(), "accept", Object.class, Consumer.class, Object.class);

        /** The method handle that was unreflected. */
        private final MethodHandle consumer;

        /** The ExecutableFactor or FieldFactory to delegate to. */
        private final Factory<T> delegate;

        public PeekableFactory(Factory<T> delegate, Consumer<? super T> action) {
            super(delegate.typeLiteral());
            this.delegate = delegate;
            MethodHandle mh = ACCEPT.bindTo(requireNonNull(action, "action is null"));
            this.consumer = MethodHandles.explicitCastArguments(mh, MethodType.methodType(rawType(), rawType()));
        }

        /** {@inheritDoc} */
        @Override
        public List<InternalDependency> dependencies() {
            return dependencies(delegate);
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle toMethodHandle(Lookup lookup) {
            MethodHandle mh = toMethodHandle0(delegate, lookup);
            mh = MethodHandles.filterReturnValue(mh, consumer);
            return MethodHandleUtil.castReturnType(mh, rawType());
        }

        @SuppressWarnings({ "unchecked", "unused", "rawtypes" })
        private static Object accept(Consumer consumer, Object object) {
            consumer.accept(object);
            return object;
        }
    }
}
