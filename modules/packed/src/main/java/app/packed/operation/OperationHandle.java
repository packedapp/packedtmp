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
package app.packed.operation;

import static java.util.Objects.checkIndex;
import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.bean.BeanIntrospector.OnBinding;
import app.packed.bean.BeanIntrospector.OnField;
import app.packed.bean.BeanIntrospector.OnMethod;
import app.packed.bean.InstanceBeanConfiguration;
import app.packed.container.Extension;
import internal.app.packed.bean.IntrospectedBean;
import internal.app.packed.bean.IntrospectedBeanBinding;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.PackedInvocationType;

/**
 * An operation handle is direct reference to an underlying method, constructor, field, or similar low-level operation.
 * <p>
 * Operation handles are the main way in which the framework supports such as annotations on fields and methods.
 * <p>
 * An operation handle can be constructed by an {@link Extension extension} in any of the following ways:
 * <ul>
 * <li>By calling {@link OnMethod#newOperation} to create a new operation that can {@code invoke} the underlying
 * {@link Method}.
 * <li>By calling {@link OnField#newGetOperation} to create a new operation that can {@code get} the value of the
 * underlying {@link Field}.
 * <li>By calling {@link OnField#newSetOperation} to create a new operation that car {@code set} the value of the
 * underlying {@link Field}.
 * <li>By calling {@link OnField#newOperation(java.lang.invoke.VarHandle.AccessMode)} to create a new operation that can
 * {@code access} the underlying {@link Field}.
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
 * @see OnMethod#newOperation()
 * @see OnField#newGetOperation()
 * @see OnField#newSetOperation()
 * @see OnField#newOperation(java.lang.invoke.VarHandle.AccessMode)
 */

/// Configuration -> Set InvocationType, Set InvocationBean, Set Context
public final class OperationHandle {

    /** The bean that is being introspected. */
    private final IntrospectedBean iBean;

    /** The wrapped operation. */
    private final OperationSetup operation;

    /**
     * Creates a new handle.
     * 
     * @param operation
     *            the operation to wrap
     */
    OperationHandle(OperationSetup operation, IntrospectedBean iBean) {
        this.operation = requireNonNull(operation);
        this.iBean = requireNonNull(iBean);
    }

    /**
     * <p>
     * This operation is no longer configurable when this method returns.
     * 
     * @param parameterIndex
     *            the index of the parameter to bind
     * @return an object that can be used to bind the parameter with the specified index
     * @throws IndexOutOfBoundsException
     *             if the parameter index is out of bounds
     */
    public OnBinding bindManually(int parameterIndex) {
        // This method does not throw IllegalStateExtension, but OnBinding may.
        operation.isClosed = true;
        // custom invocationContext must have been set before calling this method
        checkIndex(parameterIndex, operation.site.type.parameterCount());
        return new IntrospectedBeanBinding(iBean, operation, parameterIndex, operation.operator, null, operation.site.type.parameter(parameterIndex));
    }

    /**
     * Checks that operation is still configurable
     */
    private void checkConfigurable() {
        if (operation.isClosed) {
            throw new IllegalStateException("This operation is no longer configurable");
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof OperationHandle h && operation == h.operation;
    }

    /**
     * Generates a method handle that can be used to invoke the underlying operation.
     * <p>
     * Taking into account the invocation type and site.
     * <p>
     * The method type of the returned method handle will be {@code invocationType().methodType()}.
     * 
     * @return the generated method handle
     * 
     * @throws IllegalStateException
     *             if called more than once. Or if called outside of the code generating phase of the application
     */
    public MethodHandle generateMethodHandle() {
        return operation.generateMethodHandle();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return operation.hashCode();
    }

    public int injectMethodHandleArrayInto(InstanceBeanConfiguration<?> bean) {
        requireNonNull(bean, "bean is null");

        // Hvorfor maa man egentlig ikke det her???
        // Vi kan jo altid bare lave vores egen og injecte den...
        if (bean.owner().isApplication() || bean.owner().extension() != operation.operator.extensionType) {
            throw new IllegalArgumentException("Can only specify a bean that has extension " + operation.operator.extensionType.getSimpleName() + " as owner");
        }
        return operation.site.bean.container.application.codegenHelper.addArray(bean, this);
    }

    public <T> void injectMethodHandleMapInto(InstanceBeanConfiguration<?> bean, Class<T> keyType, T key) {
        // check is type
        throw new UnsupportedOperationException();
    }

    /** {@return the invocation type of this operation.} */
    public InvocationType invocationType() {
        return operation.pit;
    }

    public void invokeAs(InvocationType invocationType) {
        requireNonNull(invocationType, "invocationType is null");
        checkConfigurable();
        operation.pit = (PackedInvocationType) invocationType;
    }

    // Kan kaldes en gang
    // ExtensionContext instead???
    //
    public void invokeFrom(InstanceBeanConfiguration<?> bean) {
        // Vi kan have et application.HashMap<BeanSetup, Map<Key, Supplier>> delayedCodegen;
        // Som de bruger.
        // BeanSetup.codegenInjectIntoBean(IBC<?>, Key, Supplier);
        throw new UnsupportedOperationException();
    }

    public void named(String name) {
        requireNonNull(name, "name is null");
        checkConfigurable();
        operation.name = name;
    }

    /**
     * Registers an action that will be performed doing the code generation phase of the application.
     * <p>
     * This method is typically used for storing the method handle returned by {@link #generateMethodHandle()} in some kind
     * of data structure.
     * 
     * @param action
     *            the action to perform
     * @throws IllegalStateException
     *             if the operation is no longer configurable
     */
    public void onCodegen(Consumer<OperationHandle> action) {
        requireNonNull(action, "action is null");
        checkConfigurable();
        operation.site.bean.container.application.addCodegenAction(() -> action.accept(this));
    }

    /**
     * Specializes the mirror that is returned for the operation.
     * <p>
     * The specified supplier may be called multiple times for the same operation.
     * <p>
     * The specified supplier should never return {@code null}.
     * 
     * @param supplier
     *            a mirror supplier that is only called if a mirror is needed
     * @throws IllegalStateException
     *             if the operation is no longer configurable
     */
    // I don't know if have a forceMirrorBaseType??? Basic idea being that if delegate the operation
    // to someone they cannot specialize with a mirror that is not a subtype of the specified type
    // For example, LifetimeOperationMirror.
    // However, the best thing we can do is a runtime exception. As the supplier is lazy
    public void specializeMirror(Supplier<? extends OperationMirror> supplier) {
        checkConfigurable();
        operation.mirrorSupplier = requireNonNull(supplier, "supplier is null");
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return operation.toString();
    }

    /** {@return the type of this operation.} */
    public OperationType type() {
        return operation.site.type;
    }

}

interface OpNew {

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

    default void spawnNewBean() {
        // I'm not sure this is needed.
        // It is always only configured on the bean

        // Can't see we will ever need to combi it

        // A new bean will be created. I think we need to configure something when making the bean as well
        // Maybe we need a bean option and call this method for every operation.
        // I don't know can we have methods that can do both
    }

}

interface ZandboxOperationHandle {

    default boolean hasBindingsBeenResolved() {
        return false;
    }

    default <F, T> OperationHandle mapReturn(Class<F> fromType, Class<T> toType, Function<F, T> function) {
        // Vi kan fx sige String -> StringReturnWrapper
        throw new UnsupportedOperationException();
    }

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

    // dependencies skal vaere her, fordi de er mutable. Ved ikke om vi skal have 2 klasser.
    // Eller vi bare kan genbruge BeanDependency

    default void resolveBindings() {
        // we need the introspected bean...

        // Hmm, I don't like it

        // If we don't call this ourselves. It will be called immediately after
        // onMethod, onField, ect
    }

    Sandbox resultAssignableToOrFail(Class<?> clz); // ignores any return value

    Sandbox resultVoid(); // returnIgnore?

    Sandbox resultVoidOrFail(); // fails if non-void with BeanDeclarationException

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