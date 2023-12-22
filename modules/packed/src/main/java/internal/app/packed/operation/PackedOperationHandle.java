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
package internal.app.packed.operation;

import static java.util.Objects.checkIndex;
import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.component.ComponentPath;
import app.packed.extension.BindableVariable;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionContext;
import app.packed.operation.Op;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationTarget;
import app.packed.operation.OperationType;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanScanner;
import internal.app.packed.bean.PackedBindableVariable;
import sandbox.extension.operation.OperationHandle;
import sandbox.extension.operation.OperationTemplate;

/**
 * An operation handle is direct reference to an underlying method, constructor, field, or similar low-level operation.
 * <p>
 * Operation handles are the main way in which the framework supports such as annotations on fields and methods.
 * <p>
 * An operation handle can be constructed by an {@link Extension extension} in any of the following ways:
 * <ul>
 * <li>By calling {@link OperationalMethod#newOperation} to create a new operation that can {@code invoke} the
 * underlying {@link Method}.
 * <li>By calling {@link OperationalField#newGetOperation} to create a new operation that can {@code get} the value of
 * the underlying {@link Field}.
 * <li>By calling {@link OperationalField#newSetOperation} to create a new operation that car {@code set} the value of
 * the underlying {@link Field}.
 * <li>By calling {@link OperationalField#newOperation(java.lang.invoke.VarHandle.AccessMode)} to create a new operation
 * that can {@code access} the underlying {@link Field}.
 * </ul>
 *
 *
 * This class is used to configure a operation.
 *
 * This class contains a number of configuration methods:
 *
 * List them
 *
 *
 *
 * This class currently supports:
 * <ul>
 * <li>Creating method handles that can invoke the underlying operation.</li>
 * <li>Special processing of dependencies.</li>
 * <li>Specialize the mirror that is exposed for the operation.</li>
 * </ul>
 * <p>
 * This class must support:
 * <ul>
 * <li>Customization of the argument side (lanes). For example, take a SchedulingContext as argument X.</li>
 * <li>Name the operation (not implemented yet).</li>
 * </ul>
 * <p>
 *
 * @see OperationalMethod#newOperation()
 * @see OperationalField#newGetOperation()
 * @see OperationalField#newSetOperation()
 * @see OperationalField#newOperation(java.lang.invoke.VarHandle.AccessMode)
 */

// interceptor().add(...);
// interceptor().peek(e->System.out.println(e));
public final record PackedOperationHandle(OperationSetup operation, @Nullable BeanScanner s) implements OperationHandle {

    /** {@inheritDoc} */
    @Override
    public OperationTarget target() {
        return operation.target();
    }

    /** {@inheritDoc} */
    // Tror vi force laver (reserves) en binding her.
    // Det er jo kun meningen at man skal binden den hvis man kalder denne metode.
    // Saa maaske skal vi have en Mode i IBB

    // overrideParameter?
    // bindParameter
    // bindManually
    // bind(index).toConstant("Foo");
    // Maybe take an consumer to make sure it is "executed"
    @Override
    public BindableVariable manuallyBindable(int index) {

        // This method does not throw IllegalStateExtension, but OnBinding may.
        // custom invocationContext must have been set before calling this method
        checkIndex(index, operation.type.parameterCount());
        if (s == null) {
            throw new UnsupportedOperationException();
        }
        // TODO we need to check that s is still active
        return new PackedBindableVariable(s, operation, index, operation.operator, operation.type.parameter(index));
    }


    /** {@inheritDoc} */
    @Override
    public MethodHandle generateMethodHandle() {
        return operation.generateMethodHandle();
    }

    /** {@inheritDoc} */
    @Override
    public MethodType invocationType() {
        return operation.template.descriptor().invocationType();
    }

    // Ogsaa en template ting taenker jeg? IDK
    /** {@inheritDoc} */
    @Override
    public void named(String name) {
        requireNonNull(name, "name is null");
        checkIsConfigurable();
        operation.namePrefix = name;
    }

    /** {@inheritDoc} */
    @Override
    public void specializeMirror(Supplier<? extends OperationMirror> supplier) {
        checkIsConfigurable();
        operation.mirrorSupplier = requireNonNull(supplier, "supplier is null");
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "OperationHandle: " + operation.toString();
    }

    /** {@inheritDoc} */
    @Override
    public OperationType type() {
        return operation.type;
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends Extension<?>> operator() {
        return operation.operator.extensionType;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath componentPath() {
        return operation.componentPath();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConfigurable() {
        return operation.operator.isConfigurable();
    }
}

interface ZandboxOH {

    // Must not have a classifier
    //// Will have a MH injected at runtime...
    // public void generateInto(InstanceBeanConfiguration<?> bean) {
    // generateInto(bean, Key.of(MethodHandle.class));
    // }
    //
    // public void generateInto(InstanceBeanConfiguration<?> bean, Key<MethodHandle> key) {
    // // En anden mulighed er at det er bundet i InvocationSite...
    // throw new UnsupportedOperationException();
    // }
    //
    //// Must not have a classifier
    //// Will have a MH injected at runtime...
    //
    // public <T> T generateIntoWithAutoClassifier(InstanceBeanConfiguration<?> bean, Class<T> classifierType) {
    // return generateIntoWithAutoClassifier(bean, Key.of(MethodHandle.class), classifierType);
    // }
    //
    // public <T> T generateIntoWithAutoClassifier(InstanceBeanConfiguration<?> bean, Key<MethodHandle> key, Class<T>
    // classifierType) {
    // throw new UnsupportedOperationException();
    // }
    //
    // public void generateIntoWithClassifier(InstanceBeanConfiguration<?> bean, Key<MethodHandle> key, Object classifier) {
    // // InvocationType must have a classifier with an assignable type
    // }
    //
    // public void generateIntoWithClassifier(InstanceBeanConfiguration<?> bean, Object classifier) {
    // generateIntoWithClassifier(bean, Key.of(MethodHandle.class), classifier);
    // }
    //
//     public void injectMethodHandleArrayInto(InstanceBeanConfiguration<?> bean, Object classifier) {
//         throw new UnsupportedOperationException();
//     }
    //
//     public int injectMethodHandleArrayInto(InstanceBeanConfiguration<?> bean) {
//         requireNonNull(bean, "bean is null");
    //
//         // Hvorfor maa man egentlig ikke det her???
//         // Vi kan jo altid bare lave vores egen og injecte den...
//         if (bean.owner().isApplication() || bean.owner().extension() != operation.operator.extensionType) {
//             throw new IllegalArgumentException("Can only specify a bean that has extension " + operation.operator.extensionType.getSimpleName() + " as owner");
//         }
//         return operation.bean.container.application.codegenHelper.addArray(bean, this);
//     }
    //
//     public <T> void injectMethodHandleMapInto(InstanceBeanConfiguration<?> bean, Class<T> keyType, T key) {
//         // check is type
//         throw new UnsupportedOperationException();
//     }
    default <T> void attach(Class<T> t, T value) {
        // detach <- removes
        // Skal vi remove??? Taenker kun det er noget vi kan goere mens vi builder
        // Det er ikke noget der giver mening senere hen
        // Gem i et map i application <OperationHandle, Class> -> Value
    }

    default boolean hasBindingsBeenResolved() {
        return false;
    }

    // Hmm, kan jo ikke bare tage en tilfaeldig...
    default void invokeFromAncestor(Extension<?> extension) {}

    default void invokeFromAncestor(ExtensionContext context) {}

    // Can be used to optimize invocation...
    // Very advanced though
    // Ideen er nok at vi har den model hvad der er til raadighed...
    // Hvilke argumenter skal du egentlig bruge...
    boolean isInvocationArgumentUsed(int index);

    default <F, T> OperationHandle mapReturn(Class<F> fromType, Class<T> toType, Function<F, T> function) {
        // Vi kan fx sige String -> StringReturnWrapper
        throw new UnsupportedOperationException();
    }

    // Hvad hvis vi vil injecte ting??? Return is always the first parameter I would think
    // Additional parameters will be like any other bindings
    // Will it create an additional operation? I would think so if it needs injection
    default OperationHandle mapReturn(MethodHandle mh) {
        // Vi kan fx sige String -> StringReturnWrapper
        throw new UnsupportedOperationException();
    }

    // non void return matching invocation type
    default OperationHandle mapReturn(Op<?> op) {
        // Vi kan fx sige String -> StringReturnWrapper
        throw new UnsupportedOperationException();
    }

    // We have on the extension now
    void onCodegen(Consumer<OperationHandle> action);

    // I think this needs to be first operation...
    // Once we start calling onBuild() which schedules it for the extension its over
    default void operatedBy(Object extensionHandle) {
        // delegateTo, transferTo
        // Maybe the method is on ExtensionPoint.UseSite
        // checkConfigurable();
        // Do we create a new handle, and invalidate this handle?
    }

    default void relativise(Extension<?> extension) {

    }

    // I don't know if have a forceMirrorBaseType??? Basic idea being that if delegate the operation
    // to someone they cannot specialize with a mirror that is not a subtype of the specified type
    // For example, LifetimeOperationMirror.
    // However, the best thing we can do is a runtime exception. As the supplier is lazy
    void requireMirrorSubClassOf(Class<? extends OperationMirror> mclass);

    // /**
//  * If this operation is created from a variable (typically a field), returns its accessMode. Otherwise empty.
//  *
//  * @return
//  */
// default Optional<AccessMode> accessMode() {
//     return Optional.empty();
// }
//    default <T> T computeInvoker(TypeToken<T> invokerType) {
//
//        /// computeInvoker(new TypeToken<Function<Boo, Sddd>);
//        throw new UnsupportedOperationException();
//    }

    default void resolveParameter() {
        // we need the introspected bean...

        // Hmm, I don't like it

        // If we don't call this ourselves. It will be called immediately after
        // onMethod, onField, ect
    }

    ZandboxOH resultAssignableToOrFail(Class<?> clz); // ignores any return value

    // dependencies skal vaere her, fordi de er mutable. Ved ikke om vi skal have 2 klasser.
    // Eller vi bare kan genbruge BeanDependency

    // Resolves all parameters that have not already been resolved via bindParameter
    // Idea was to act on all those that werent resolved

    // boolean isBound(int parameterIndex)

    ZandboxOH resultVoid(); // returnIgnore?

    ZandboxOH resultVoidOrFail(); // fails if non-void with BeanDeclarationException

    // spawn NewBean/NewContainer/NewApplication...
    default void spawnNewBean() {
        // I'm not sure this is needed.
        // It is always only configured on the bean

        // Can't see we will ever need to combi it

        // A new bean will be created. I think we need to configure something when making the bean as well
        // Maybe we need a bean option and call this method for every operation.
        // I don't know can we have methods that can do both
    }

    // If immutable... IDK
    // Fx fra mirrors. Har composite operations ogsaa templates???
    OperationTemplate template();

    //
//  private static <K, U, V> Map<K, U> copyOf(Map<K, V> map, Function<V, U> valueMapper) {
////      Map<K, U> result = map.entrySet().stream().collect(Collectors.toMap(Entry::getKey, valueMapper));
////      return Map.copyOf(result);
//      throw new UnsupportedOperationException();
//  }
//
//  public static <B extends InstanceBeanConfiguration<?>> B initializeWithMethodHandleArray(B bean, Collection<OperationHandle> operations) {
//      return bean;
//  }
//
//  public static <B extends InstanceBeanConfiguration<?>> B initializeWithMethodHandleArray(B bean, OperationHandle[] operations) {
//      return bean;
//  }
//
//  public static <B extends InstanceBeanConfiguration<?>, K> B initializeWithMethodHandleMap(B bean, Key<Map<K, MethodHandle>> key,
//          Map<K, OperationHandle> operations) {
//      bean.overrideServiceDelayed(key, () -> copyOf(operations, h -> h.buildInvoker()));
//      return bean;
//  }
//
//  public static Supplier<MethodHandle[]> supplier(OperationHandle[] operations) {
//      return () -> {
//          MethodHandle[] mhs = new MethodHandle[operations.length];
//          for (int i = 0; i < operations.length; i++) {
//              mhs[i] = operations[i].buildInvoker();
//          }
//          return mhs; // .freeze();
//      };
//  }
//
//  public static <K> Supplier<Map<K, MethodHandle>> supplierMap(Map<K, OperationHandle> operations) {
//      return () -> {
//          throw new UnsupportedOperationException();
//      };
//  }
}

// Hvad goer vi med annoteringer paa Field/Update???
// Putter paa baade Variable og ReturnType???? Det vil jeg mene

// Ideen er lidt at hvis vi har forskel

// Eller har vi OperationConfiguration<T>, og saa bestemer man typen naar
// laver operationen

//Operation"Type"
//Bean instance provided from OperationPack vs Bean Instance provided as MH.parameter

//checks() are always performed before we create an actual operation

//Vi skal have en eller anden form for naming.
//int OperationID?
//noget omkring Instance (requiresInstance) Altsaa det er lidt
//noget omkring wrapping mode

//FactoryInvoker??? Fraekt hvis faa dem bundet sammen

//Kan laves fra et Field eller Method
//og kan invokere en metoder/constructor, lase/skrive/update et field

//To primaere funktioner...
/// Injection, MH creation

/// Styring omkring

//Lad os sige vi godt vil generere en hidden class extension bean...

// Vi har ikke MethodType/Factory type her... Det maa komme fra BeanConstructor/BeanMethod
//

//OperationHandle??? Vi dropper builder parten... Eller maaske ikke IDK
//Vi har brug for at saette nogle ting inde vi skanner og tilfoejer operationer
//i nogen tilfaelde..
//Fx

////Sources
//* Fra MethodHook/FieldHook/InjectionHook
//* Via BeanDriver <- er det altid bare en functional faetter????

//Ved ikke fx med exportAll() <- her vil vi jo gerne capture alt paa ind gang, og ikke for hver bean
////Der tilfojere vi jo bean

//Kan godt kalde det BeanOperation... Det er ikke navne brugeren nogensinde bruger...
//Men saa BeanOperationInterceptorMirror... May be okay.

//Den her svare lidt til BeanDriver, og saa alligvl ikke

//addHttpRequest();

// Ved ikke om vi skal have <T extends OperationMirror) useMirror(Class<T> mirrorType, Supplier<T> suppler)
// Som nu skal vi aktivt lave alle mirrors hvis man fx kalder ApplicationMirror.operations(EntryPointMirror.class)
// Ved vi ikke hvilke operationer den passer på, det kan vi først finde ud af når den er lavet 