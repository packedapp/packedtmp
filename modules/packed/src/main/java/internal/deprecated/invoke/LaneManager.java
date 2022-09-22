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
package internal.deprecated.invoke;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.List;

import app.packed.base.Nullable;

/**
 *
 */
public final class LaneManager {

    final List<Lane> lanes;

    final MethodType signature;

    LaneManager(LaneManagerBuilder builder) {
        signature = MethodType.methodType(builder.returnType, builder.parameterTypes);
        this.lanes = List.copyOf(builder.lanes);
    }

    public MethodType signature() {
        return signature;
    }

    interface Lane {
        int laneIndex();

        Class<?> type();
    }

    record ParameterLane(int laneIndex, Class<?> type) implements Lane {}

    record ComputableLane(int laneIndex, MethodHandle computation, int[] laneDependencies) implements Lane {
        // not sure if we have OptionalLane dependencies????
        /** {@inheritDoc} */
        @Override
        public Class<?> type() {
            return computation.type().returnType();
        }
    }

    record OptionalLane(Lane delegate, LaneManager.OptionalLane.Sentinal sentinal, @Nullable ComputableLane fastChecker) implements Lane {

        // delegate should not be another OptionalLane
        // fastChecker must return boolean...
        /** {@inheritDoc} */
        @Override
        public int laneIndex() {
            return delegate.laneIndex();
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> type() {
            return delegate.type();
        }

        enum Sentinal {
            NULL
        }
    }
}