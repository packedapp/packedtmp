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
package packed.inject.reflect;

import java.lang.invoke.MethodHandle;

import app.packed.inject.BindingMode;
import app.packed.inject.Key;
import packed.util.descriptor.InternalMethodDescriptor;

/**
 *
 */
public class AnnotatedMethodProvides extends AnnotatedMethod {

    /**
     * @param descriptor
     * @param handle
     */
    AnnotatedMethodProvides(InternalMethodDescriptor descriptor, MethodHandle handle) {
        super(descriptor, handle);
    }

    /**
     * @return
     */
    public BindingMode getCachingMode() {
        return null;
    }

    /**
     * @return
     */
    public String getDescription() {
        return null;
    }

    /**
     * @return
     */
    public Object getMethod() {
        return null;
    }

    /**
     * @return
     */
    public Key<?> getKey() {
        return null;
    }

}
