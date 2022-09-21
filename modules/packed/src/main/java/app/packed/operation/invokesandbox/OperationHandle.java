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
package app.packed.operation.invokesandbox;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.base.TypeToken;
import app.packed.bean.BeanIntrospector$BeanField;
import app.packed.bean.BeanIntrospector$BeanMethod;
import app.packed.bean.BeanIntrospector$BeanVariableBinder;
import app.packed.container.ExtensionBeanConfiguration;
import app.packed.operation.OperationMirror;
import internal.app.packed.operation.PackedOperationHandle;

/**
 * This class currently supports:
 * <ul>
 * <li>Creating handles that can invoke the underlying operation.</li>
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
 * This class may support:
 * <ul>
 * </ul>
 * 
 * 
 * @see BeanIntrospector$BeanField#newGetOperation(ExtensionBeanConfiguration)
 * @see BeanIntrospector$BeanField#newSetOperation(ExtensionBeanConfiguration)
 * @see BeanIntrospector$BeanField#newOperation(ExtensionBeanConfiguration, java.lang.invoke.VarHandle.AccessMode)
 * @see BeanIntrospector$BeanMethod#newOperation(ExtensionBeanConfiguration)
 */
// no functionality for reading annotations, use BeanField, BeanMethod, ect
public sealed interface OperationHandle permits PackedOperationHandle {

    /**
     * @return
     * 
     * @throws IllegalStateException
     *             if called more than once. Or if called before the handle can be computed
     * @throws UnsupportedOperationException
     *             if method handle are not supported
     * @see ExtensionBeanConfiguration#bindDelayed(Class, Supplier)
     */
    MethodHandle computeMethodHandle();

    default OperationHandle mapReturn(MethodHandle mh) {
        // Vi kan fx sige String -> StringReturnWrapper
        throw new UnsupportedOperationException();
    }

    default <F, T> OperationHandle mapReturn(Class<F> fromType, Class<T> toType, Function<F, T> function) {
        // Vi kan fx sige String -> StringReturnWrapper
        throw new UnsupportedOperationException();
    }

    default InvocationType invocationType() {
        throw new UnsupportedOperationException();
    }

    default OperationHandle invokeAs(InvocationType type) {
        // This method must be called before any parameters have been bound
        return this;
    }

    // dependencies skal vaere her, fordi de er mutable. Ved ikke om vi skal have 2 klasser.
    // Eller vi bare kan genbruge BeanDependency

    /**
     * Returns a unmodifiable list of the dependencies of this operation.
     * <p>
     * These dependencies can be used to customize injection for this particular operation.
     * 
     * @return a unmodifiable list of the dependencies of this operation
     */
    // Dependencies that are not explicitly bound
    default List<BeanIntrospector$BeanVariableBinder> bindables() {
        throw new UnsupportedOperationException();
    }

    /**
     * Adds a supplier that creates the mirror that will be returned when a mirror for the operation is requested.
     * <p>
     * The supplier may be called multiple times for the same operation.
     * <p>
     * The supplier should never return {@code null}.
     * 
     * @param supplier
     *            a mirror supplier
     */
    // I don't know if have a forceMirrorBaseType??? Basic idea being that if delegate the operation
    // to someone they cannot specialize with a mirror that is not a subtype of the specified type
    // For example, LifetimeOperationMirror.
    // However, the best thing we can do is a runtime exception. As the supplier is lazy
    OperationHandle specializeMirror(Supplier<? extends OperationMirror> supplier);
}

interface Sandbox {
//  /**
//  * If this operation is created from a variable (typically a field), returns its accessMode. Otherwise empty.
//  * 
//  * @return
//  */
// default Optional<AccessMode> accessMode() {
//     return Optional.empty();
// }
    default <T> T computeInvoker(TypeToken<T> invokerType) {

        /// computeInvoker(new TypeToken<Function<Boo, Sddd>);
        throw new UnsupportedOperationException();
    }

    Sandbox resultAssignableToOrFail(Class<?> clz); // ignores any return value

    Sandbox resultVoid(); // returnIgnore?

    Sandbox resultVoidOrFail(); // fails if non-void with BeanDeclarationException
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
