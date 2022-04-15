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
package app.packed.bean.operation;

import java.util.function.Supplier;

import app.packed.bean.operation.mirror.OperationMirror;

/**
 *
 */
@SuppressWarnings("rawtypes")
// OperationHandle??? Vi dropper builder parten... Eller maaske ikke IDK
// Vi har brug for at saette nogle ting inde vi skanner og tilfoejer operationer
// i nogen tilfaelde..
// Fx 
public sealed interface Operation permits InjectableOperation, RawOperation {

    // newInstance(); <-- automatisk???

    // sets a non-generic mirror

    // Ved ikke om vi skal have <T extends OperationMirror) useMirror(Class<T> mirrorType, Supplier<T> suppler)
    // Som nu skal vi aktivt lave alle mirrors hvis man fx kalder ApplicationMirror.operations(EntryPointMirror.class)
    // Ved vi ikke hvilke operationer den passer på, det kan vi først finde ud af når den er lavet 
    default void useMirror(Supplier<? extends OperationMirror> supplier) {
        
    }
}

////Sources
//* Fra MethodHook/FieldHook/InjectionHook
//* Via BeanDriver <- er det altid bare en functional faetter????

//Ved ikke fx med exportAll() <- her vil vi jo gerne capture alt paa ind gang, og ikke for hver bean
////Der tilfojere vi jo bean

//Kan godt kalde det BeanOperation... Det er ikke navne brugeren nogensinde bruger...
//Men saa BeanOperationInterceptorMirror... May be okay.

//Den her svare lidt til BeanDriver, og saa alligvl ikke

//addHttpRequest();