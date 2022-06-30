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
import java.lang.invoke.VarHandle;
import java.util.List;
import java.util.function.Supplier;

import app.packed.operation.dependency.BeanDependency;
import internal.app.packed.operation.PackedOperationBuilder;

/**
 *
 */
// ArgumentSide (Coordinates), lanes und so weiter

// specializere mirrors
// create XHandle
// laver special handling af en eller flere dependencies
// Noget navngivning
public sealed interface OperationConfiguration permits PackedOperationBuilder {

    default List<BeanDependency> dependencies() {
        throw new UnsupportedOperationException();
    }

    // Ellers har vi OperationConfiguration<T>
    <T> T handleNow(Class<T> handleType);
    
    /**
     * @return
     * 
     * @throws IllegalStateException
     *             foo
     * @throws UnsupportedOperationException
     *             if a varhandle is created instead
     */
    default MethodHandle methodHandleNow() {
        throw new UnsupportedOperationException();
    }

    /**
     * Adds a supplier that creates a specialized operation mirror for the operation when a mirror for the operation is
     * requested.
     * <p>
     * The supplier may be called multiple times.
     * <p>
     * The supplier should never return {@code null}.
     * 
     * @param supplier
     *            a mirror supplier
     */
    // Det boer ogsaa vaere muligt at lave disse direkte hvis man har behov for dem.
    //// Fx EntryPointM
    // Eller ogsaa kan man vel bare finde dem?
    OperationConfiguration specializeMirror(Supplier<? extends OperationMirror> supplier);

    // Hvad goer vi med annoteringer paa Field/Update???
    // Putter paa baade Variable og ReturnType???? Det vil jeg mene

    // Ideen er lidt at hvis vi har forskel

    default VarHandle varHandleNow() {
        throw new UnsupportedOperationException();
    }

//    public enum Option {
//        NO_OP_PACK
//    }

//    final class RawExtractor<T> {
//        static final RawExtractor<MethodHandle> METHOD_HANDLE = null;
//        static final RawExtractor<MethodHandle> METHOD_HANDLE_GETTER = null;
//        static final RawExtractor<MethodHandle> METHOD_HANDLE_SETTER = null;
//        static final RawExtractor<MethodHandle> VAR_HANDLE = null; // buildVarHandleNow
//    }
}

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
