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
package pckd.internal.inject.reflect;

import java.lang.invoke.MethodHandle;

import app.packed.inject.BindingMode;
import app.packed.inject.Key;
import app.packed.inject.Provider;
import pckd.internal.util.descriptor.InternalMethodDescriptor;

/** This class represents a method annotated with the {@link Provider} annotation. */
public class AnnotatedMethodProvides extends AnnotatedMethod {

    AnnotatedMethodProvides(InternalMethodDescriptor descriptor, MethodHandle handle) {
        super(descriptor, handle);
    }

    public BindingMode getCachingMode() {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public Object getMethod() {
        return null;
    }

    public Key<?> getKey() {
        return null;
    }

}
