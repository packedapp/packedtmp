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
package packed.internal.inject.factory;

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
import packed.internal.inject.InternalDependency;
import packed.internal.util.LookupUtil;
import packed.internal.util.MethodHandleUtil;

/**
 *
 */
// Move TypeLiteral Out I think, then we can use records
public abstract non-sealed class InternalFactory<R> extends Factory<R> {

    public abstract List<InternalDependency> dependencies();

    public abstract MethodHandle toMethodHandle(Lookup lookup);


    public static <R> InternalFactory<R> crackFactory(Factory<R> factory) {
        requireNonNull(factory, "factory is null");
        if (factory instanceof CapturingFactory<R> f) {
            return (InternalFactory<R>) InternalCapturingInternalFactory.VH_CF_FACTORY.get(factory);
        } else {
            return (InternalFactory<R>) factory;
        }
    }

    public static final class InternalCapturingInternalFactory<R> extends InternalFactory<R> {

        /** A var handle that can update the {@link #container()} field in this class. */
        private static final VarHandle VH_CF_FACTORY = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), CapturingFactory.class, "factory",
                InternalCapturingInternalFactory.class);

        // Ideen er lidt at saa snart vi bruger et CapturingFactory saa smider vi den ind her

        /** The dependencies of this factory, extracted from the type variables of the subclass. */
        // Taenker vi laver en private record delegate der holder begge ting...
        // Og saa laeser
        public final List<InternalDependency> dependencies;

        public final MethodHandle methodHandle;

        /** The type of objects this factory creates. */
        private final TypeToken<R> typeLiteral;

        /**
         * @param typeLiteralOrKey
         */
        public InternalCapturingInternalFactory(TypeToken<R> typeLiteralOrKey, MethodHandle methodHandle, List<InternalDependency> dependencies) {
            this.typeLiteral = requireNonNull(typeLiteralOrKey, "typeLiteralOrKey is null");
            this.dependencies = dependencies;
            this.methodHandle = methodHandle;
        }

        /** {@inheritDoc} */
        @Override
        public List<InternalDependency> dependencies() {
            return dependencies;
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle toMethodHandle(Lookup lookup) {
            return methodHandle;
        }

        /** {@inheritDoc} */
        @Override
        public TypeToken<R> typeLiteral() {
            return typeLiteral;
        }
    }

    /** A special factory created via {@link #withLookup(Lookup)}. */
    // A simple version of Binding... Maybe just only have one
    public static final class BoundFactory<R> extends InternalFactory<R> {

        private final Object[] arguments;

        /** The ExecutableFactor or FieldFactory to delegate to. */
        private final InternalFactory<R> delegate;

        private final List<InternalDependency> dependencies;

        /** The ExecutableFactor or FieldFactory to delegate to. */
        private final int index;

        /** The type of objects this factory creates. */
        private final TypeToken<R> typeLiteral;

        public BoundFactory(InternalFactory<R> delegate, int index, InternalDependency[] dd, Object[] arguments) {
            this.typeLiteral = delegate.typeLiteral();
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
            MethodHandle mh = delegate.toMethodHandle(lookup);
            return MethodHandles.insertArguments(mh, index, arguments);
        }

        /** {@inheritDoc} */
        @Override
        public TypeToken<R> typeLiteral() {
            return typeLiteral;
        }
    }

    /** A factory that provides the same value every time, used by {@link Factory#ofConstant(Object)}. */
    public static final class ConstantFactory<R> extends InternalFactory<R> {

        /** The value that is returned every time. */
        private final R instance;

        /** The type of objects this factory creates. */
        private final TypeToken<R> typeLiteral;

        @SuppressWarnings("unchecked")
        public ConstantFactory(R instance) {
            this.typeLiteral = (TypeToken<R>) TypeToken.of(instance.getClass());
            this.instance = instance;
        }

        /** {@inheritDoc} */
        @Override
        public List<InternalDependency> dependencies() {
            return List.of();
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle toMethodHandle(Lookup ignore) {
            return MethodHandles.constant(instance.getClass(), instance);
        }

        /** {@inheritDoc} */
        @Override
        public TypeToken<R> typeLiteral() {
            return typeLiteral;
        }
    }

    /** A special factory created via {@link #withLookup(Lookup)}. */
    public static final class LookedUpFactory<R> extends InternalFactory<R> {

        /** The ExecutableFactor or FieldFactory to delegate to. */
        private final InternalFactory<R> delegate;

        /** The method handle that was unreflected. */
        private final MethodHandle methodHandle;

        /** The type of objects this factory creates. */
        private final TypeToken<R> typeLiteral;

        public LookedUpFactory(InternalFactory<R> delegate, MethodHandle methodHandle) {
            this.typeLiteral = delegate.typeLiteral();
            this.delegate = delegate;
            this.methodHandle = requireNonNull(methodHandle);
        }

        /** {@inheritDoc} */
        @Override
        public List<InternalDependency> dependencies() {
            return delegate.dependencies();
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle toMethodHandle(Lookup ignore) {
            return methodHandle;
        }

        /** {@inheritDoc} */
        @Override
        public TypeToken<R> typeLiteral() {
            return typeLiteral;
        }
    }

    /** A factory for {@link #peek(Consumer)}}. */
    public static final class PeekableFactory<R> extends InternalFactory<R> {

        /** A method handle for {@link Function#apply(Object)}. */
        private static final MethodHandle ACCEPT = LookupUtil.lookupStatic(MethodHandles.lookup(), "accept", Object.class, Consumer.class, Object.class);

        /** The method handle that was unreflected. */
        private final MethodHandle consumer;

        /** The ExecutableFactor or FieldFactory to delegate to. */
        private final InternalFactory<R> delegate;

        /** The type of objects this factory creates. */
        private final TypeToken<R> typeLiteral;

        public PeekableFactory(InternalFactory<R> delegate, Consumer<? super R> action) {
            this.typeLiteral = delegate.typeLiteral();
            this.delegate = delegate;
            MethodHandle mh = ACCEPT.bindTo(requireNonNull(action, "action is null"));
            this.consumer = MethodHandles.explicitCastArguments(mh, MethodType.methodType(rawReturnType(), rawReturnType()));
        }

        /** {@inheritDoc} */
        @Override
        public List<InternalDependency> dependencies() {
            return delegate.dependencies();
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle toMethodHandle(Lookup lookup) {
            MethodHandle mh = delegate.toMethodHandle(lookup);
            mh = MethodHandles.filterReturnValue(mh, consumer);
            return MethodHandleUtil.castReturnType(mh, rawReturnType());
        }

        @SuppressWarnings({ "unchecked", "unused", "rawtypes" })
        private static Object accept(Consumer consumer, Object object) {
            consumer.accept(object);
            return object;
        }

        /** {@inheritDoc} */
        @Override
        public TypeToken<R> typeLiteral() {
            return typeLiteral;
        }
    }
}
