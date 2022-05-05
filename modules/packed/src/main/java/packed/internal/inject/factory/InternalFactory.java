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
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.base.Nullable;
import app.packed.base.TypeToken;
import app.packed.base.Variable;
import app.packed.inject.CapturingFactory;
import app.packed.inject.Factory;
import packed.internal.inject.InternalDependency;
import packed.internal.util.LookupUtil;
import packed.internal.util.MethodHandleUtil;

/**
 *
 */
public abstract non-sealed class InternalFactory<R> extends Factory<R> {

    /** {@inheritDoc} */
    public final Factory<R> bind(int position, @Nullable Object argument, @Nullable Object... additionalArguments) {
        requireNonNull(additionalArguments, "additionalArguments is null");
        
        List<InternalDependency> dependencies = dependencies();

        List<Variable> variables = variables();

        Objects.checkIndex(position, dependencies.size());
        int len = 1 + additionalArguments.length;
        int newLen = dependencies.size() - len;
        if (newLen < 0) {
            throw new IllegalArgumentException(
                    "Cannot specify more than " + (len - position) + " arguments for position = " + position + ", but arguments array was size " + len);
        }

        // Removing dependencies that are being replaced

        Variable[] vars = new Variable[newLen];
        InternalDependency[] dd = new InternalDependency[newLen];
        for (int i = 0; i < position; i++) {
            dd[i] = dependencies.get(i);
            vars[i] = variables.get(i);
        }
        for (int i = position; i < dd.length; i++) {
            dd[i] = dependencies.get(i + len);
            vars[i] = variables.get(i + len);
        }

        // Populate final argument array
        Object[] args = new Object[len];
        args[0] = argument;
        for (int i = 0; i < additionalArguments.length; i++) {
            args[i + 1] = additionalArguments[i];
        }

        // TODO check types...

        return new BoundFactory<>(this, position, dd, List.of(vars), args);
    }
    
    public abstract List<InternalDependency> dependencies();
    
    public final Factory<R> peek(Consumer<? super R> action) {
        return new PeekableFactory<>(this, action);
    }

    public abstract MethodHandle toMethodHandle(Lookup lookup);

    /** {@return The variables this factory takes.} */
    public List<Variable> variables() {
        throw new UnsupportedOperationException();
    }

    public static <R> InternalFactory<R> crackFactory(Factory<R> factory) {
        requireNonNull(factory, "factory is null");
        if (factory instanceof InternalFactory<R> f) {
            return f;
        } else {
            return (InternalFactory<R>) InternalCapturingInternalFactory.VH_CF_FACTORY.get(factory);
        }
    }

    /** {@return The number of variables this factory takes.} */

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

        private final List<Variable> variables;

        public BoundFactory(InternalFactory<R> delegate, int index, InternalDependency[] dd, List<Variable> variables, Object[] arguments) {
            this.typeLiteral = delegate.typeLiteral();
            this.index = index;
            this.delegate = requireNonNull(delegate);
            this.arguments = arguments;
            this.variables = variables;
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

        @Override
        public int variableCount() {
            return variables.size();
        }

        @Override
        public List<Variable> variables() {
            return variables;
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

        @Override
        public int variableCount() {
            return 0;
        }

        @Override
        public List<Variable> variables() {
            return List.of();
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

        /** {@inheritDoc} */
        @Override
        public int variableCount() {
            return dependencies.size();
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

        /** {@inheritDoc} */
        @Override
        public int variableCount() {
            return delegate.variableCount();
        }

        /** {@inheritDoc} */
        @Override
        public List<Variable> variables() {
            return delegate.variables();
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

        /** {@inheritDoc} */
        @Override
        public TypeToken<R> typeLiteral() {
            return typeLiteral;
        }

        /** {@inheritDoc} */
        @Override
        public int variableCount() {
            return delegate.variableCount();
        }

        /** {@inheritDoc} */
        @Override
        public List<Variable> variables() {
            return delegate.variables();
        }

        @SuppressWarnings({ "unchecked", "unused", "rawtypes" })
        private static Object accept(Consumer consumer, Object object) {
            consumer.accept(object);
            return object;
        }
    }
}
//TODO Qualifiers on Methods, Types together with findInjectable????
//Yes need to pick those up!!!!
//probably rename defaultKey to key.

//Split-module class hierachies, must

//Factories only
//
//Is it the responsibility of the factory or the injector to inject fields and methods???
//+ Factory
//
//+ Injector
//Then we can disable it on a case to case basis
//You can actually use factories without injection
//-------------------------
//ServiceDescriptor
//Refereres fra InjectorDescriptor....
//Skal bruges til Filtrering... Men hvis noeglerne er skjult kan vi vel bruge service....

//Does this belong in app.packed.service????
//No because components also uses it...

//This class used to provide some bind methods...
//But we don't do that no more. Because it was just impossible to
//see what was what...
////////TYPES (Raw)
//ExactType... -> Instance, Constructor
//LowerBoundType, Field, Method
//PromisedType -> Fac0,Fac1,Fac2,

/// TypeLiteral<- Always the promised, key must be assignable via raw type
///////////////

//TypeLiteral
//actual type

//Correctness
//Instance -> Lowerbound correct, upper correct
//Executable -> Lower bound maybe correct (if exposedType=return type), upper correct if final return type
//Rest, unknown all
//Bindable -> has no effect..

//static {
//Dependency.of(String.class);// Initializes InternalApis for InternalFactory
//}

//Ideen er her. at for f.eks. Factory.of(XImpl, X) saa skal der stadig scannes paa Ximpl og ikke paa X

///**
//* Returns the injectable type of this factory. This is the type that will be used for scanning for scanning for
//* annotations. This might differ from the actual type, for example, if {@link #mapTo(Class, Function)} is used
//*
//* @return stuff
//*/
////We should make this public...
////InjectableType
//Class<? super T> scannableType() {
//  return rawType();
//}

///** {@inheritDoc} */
//@Override
//public final <S> Factory<T> bind(Class<S> key, @Nullable S instance) {
//
//// Do we allow binding non-matching keys???
//// Could be useful from Prime annotations...
//
//// Tror vi skal have to forskellige
//
//// bindParameter(int index).... retains index....
//// Throws
//
//// bindWithKey();
//
//// bindRaw(); <---- Only takes a class, ignores nullable.....
//
//// Hvordan klarer vi Foo(String firstName, String lastName)...
//// Eller
//
//// Hvordan klarer vi Foo(String firstName, SomeComposite sc)...
//
//// Det eneste der er forskel er parameter index'et...
//// Maaske bliver man bare noedt til at lave en statisk metoder....
//
//// Skal vi have en speciel MemberFactory?????
//
////
//
//// bindTo? Det er jo ikke et argument hvis det f.eks. er et field...
//
//// resolveDependency()...
//// Its not really an argument its a dependency that we resolve...
//
//// withArgumentSupplier
//throw new UnsupportedOperationException();
//}

///** {@inheritDoc} */
////Required/Optional - Key - Variable?
////Requirement
//

//Problemet med at fjerne ting fra #variables() er at saa bliver index'et lige pludselig aendret.
//F.eks. for dooo(String x, String y)
//Og det gider vi ikke....
//Saa variables stay the same -> Why shouldn't we able to bind them...

//Maaske er index ligegyldigt...
//Og det er bare en speciel mode for MethodSidecar
//Hvor man kan sige jeg tager denne variable ud af ligningen...

//Maybe add isVariableBound(int index)

//Rebinding? Ja hvorfor ikke... maaske have en #unbindable()

//Har vi en optional MemberDescriptor?????

//Hvis man nu vil injecte en composite....