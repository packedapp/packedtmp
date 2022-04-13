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
package packed.internal.inject.invoke;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;

import packed.internal.inject.invoke.LaneManager.ComputableLane;
import packed.internal.inject.invoke.LaneManager.Lane;
import packed.internal.inject.invoke.LaneManager.ParameterLane;

/**
 *
 */
public class LaneManagerBuilder {

    final ArrayList<Lane> lanes = new ArrayList<>();

    final ArrayList<Class<?>> parameterTypes = new ArrayList<>(4);

    // Hvornaar vil man aendre denne?
    Class<?> returnType = void.class;

    public int addParameterLane(Class<?> parameterType) {
        requireNonNull(parameterType, "parameterType is null");
        if (parameterType == void.class) {
            throw new IllegalArgumentException("parameterType cannot be void");
        }
        int laneIndex = lanes.size() + 1;
        lanes.add(new ParameterLane(laneIndex, parameterType));
        return laneIndex;
    }

    public int addMapping(MethodHandle computation, int... dependencies) {
        requireNonNull(computation, "computation is null");
        for (int i = 0; i < dependencies.length; i++) {
            Lane l = lanes.get(dependencies[i]);
            System.out.println(l);
            // check Assignable... or perform type conversion in place
        }
        int laneIndex = lanes.size() + 1;
        lanes.add(new ComputableLane(laneIndex, computation, dependencies));
        return laneIndex;
    }

    public LaneManagerBuilder changeReturnType(Class<?> returnType) {
        this.returnType = requireNonNull(returnType);
        return this;
    }
}
