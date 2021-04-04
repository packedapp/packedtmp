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
package packed.internal.invoke;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;

/**
 *
 */
public final class InstantiatorBuilder {

    final Executable executable;

    final MethodHandleBuilder mh;

    final OpenClass oc;

    InstantiatorBuilder(OpenClass oc, MethodHandleBuilder mh, Executable executable) {
        this.oc = requireNonNull(oc);
        this.mh = mh;
        this.executable = requireNonNull(executable);
    }

    public Class<?> type() {
        return oc.type();
    }

    public MethodHandle build() {
        return mh.build(oc, executable);
    }

    public OpenClass oc() {
        return oc;
    }

    public static InstantiatorBuilder of(MethodHandles.Lookup lookup, Class<?> implementation, Class<?>... parameterTypes) {
        OpenClass oc = OpenClass.of(lookup, implementation);
        MethodHandleBuilder mhb = MethodHandleBuilder.of(implementation, parameterTypes);
        Constructor<?> constructor = FindInjectableConstructor.get(implementation, false, e -> new IllegalArgumentException(e));
        return new InstantiatorBuilder(oc, mhb, constructor);
    }
}
