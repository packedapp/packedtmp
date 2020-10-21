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
package packed.internal.introspection;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

import packed.internal.util.ReflectionUtil;

public abstract class PackedExecutableDescriptor {

    /** The executable */
    final Executable executable;

    /** An array of the parameter descriptor for this executable */
    private final PackedParameterDescriptor[] parameters;

    /**
     * Creates a new descriptor from the specified executable.
     *
     * @param executable
     *            the executable to mirror
     */
    PackedExecutableDescriptor(Executable executable) {
        this.executable = executable;
        // Create these lazily...
        Parameter[] parameters = executable.getParameters();
        this.parameters = new PackedParameterDescriptor[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            this.parameters[i] = new PackedParameterDescriptor(this, parameters[i], i);
        }

    }

    public Executable copyExecutable() {
        return ReflectionUtil.copy(executable);
    }

    public final PackedParameterDescriptor[] getParametersUnsafe() {
        return parameters;
    }
}
