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

import java.lang.invoke.MethodHandle;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.bean.BeanIntrospector.OnBinding;
import app.packed.bean.BeanIntrospector.OnField;
import app.packed.bean.BeanIntrospector.OnMethod;
import app.packed.bean.InstanceBeanConfiguration;
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
 * @see OnField#newGetOperation(InstanceBeanConfiguration)
 * @see OnField#newSetOperation(InstanceBeanConfiguration)
 * @see OnField#newOperation(InstanceBeanConfiguration, java.lang.invoke.VarHandle.AccessMode)
 * @see OnMethod#newOperation(InstanceBeanConfiguration)
 */
// Must be used within #onX I would think????
public sealed interface OperationHandle permits PackedOperationHandle {
 
    default void onBuild(Consumer<MethodHandle> action) {
        
    }
    
    /**
     * 
     * <p>
     * The method type of the returned method handle will be {@code invocationType().methodType()}.
     * 
     * @return
     * 
     * @throws IllegalStateException
     *             if called more than once. Or if called before the handle can be computed
     * @throws UnsupportedOperationException
     *             if method handle are not supported
     * @see ExtensionBeanConfiguration#initializeWithDelayed(Class, Supplier)
     */
    MethodHandle buildInvoker(); // was computeMethodHandle()?
    
    /** {@return the invocation type of this operation.} */
    InvocationType invocationType();

    OnBinding parameter(int index);

    default OperationHandle spawnNewBean() {
        // I'm not sure this is needed. 
        // It is always only configured on the bean
        
        // Can't see we will ever need to combi it
        
        // A new bean will be created. I think we need to configure something when making the bean as well
        // Maybe we need a bean option and call this method for every operation.
        // I don't know can we have methods that can do both
        return this;
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

    /** {@return the type of this operation.} */
    OperationType type();

    public interface Option {
        static Option spawnBean() {
            throw new UnsupportedOperationException();
        }
    }
}

interface ZandboxOperationHandle {

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

    // dependencies skal vaere her, fordi de er mutable. Ved ikke om vi skal have 2 klasser.
    // Eller vi bare kan genbruge BeanDependency

// non void return matching invocation type
    default OperationHandle mapReturn(Op<?> op) {
        // Vi kan fx sige String -> StringReturnWrapper
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
