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
package app.packed.bean.hooks;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Optional;

import app.packed.base.Key;
import app.packed.bean.operation.OperationMirror;

/**
 *
 */
// MirrorType

// MethodType InputType 

// <T> -> <MethodHandle> .. 
// saa man laver new <T> Op(Driver<T>, Consumer<T>)??
public interface BeanOperationDriver {
    
    // adapt() <-- "samme" driver men med input tweaks?
    
    /** {@return type of input for the operation.} */
    MethodType inputType();

    Optional<Class<? extends OperationMirror>> mirrorType();

    // Object generateFunctionalType(TypeLiteral<T>)

    static BeanOperationDriver.Builder builder() {
        throw new UnsupportedOperationException();
    }

    interface Builder {
        
        // Looks up in the beans context
        LaneConfiguration addBeanLookup(Class<?> key);

        LaneConfiguration addBeanLookup(Key<?> key); // Ideen er at vi kan slaa services op vi kan bruge i andre method handles...

        LaneConfiguration addArgument(Class<?> type);
        
        LaneConfiguration addTemplatedArgument(Class<?> type); // uses @Provide

        LaneConfiguration addComputed(MethodHandle methodHandle, int... dependencies);

        // @ExtractHeader("msg-id")
        // Skal vaere en Runnable. kan maaske ogsaa vaere noget paa BeanOperation
        
        void onAnnotation(Class<? extends Annotation> a, Runnable runnable);

        // inputType
        // spawnNewThread
    }

    interface LaneConfiguration {

        int laneIndex();  //isLaneRequired

        Class<?> laneType();

        List<Integer> dependencies();

        // ellers har vi provide(laneId, Key);
        LaneConfiguration provideAs(Class<?> key);

        LaneConfiguration provideAs(Key<?> key);

        // INPUT

        // Naar man har computed en MH kan man se om en lane er aktiv...
        // Fx bliver PackedSchedulingContext lanen brugt... Hvis ikke kan man
        // bare give null med
    }

    // Result er ikke en lane... de operationer der er paa LaneConfiguration giver ikke mening
    enum LaneKind {
        ARGUMENT, LOOKUP, COMPUTED
    }
}
