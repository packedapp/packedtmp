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
package packed.internal.invokers;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

import app.packed.inject.IllegalAccessRuntimeException;
import packed.internal.util.descriptor.InternalFieldDescriptor;
import packed.internal.util.descriptor.InternalMethodDescriptor;

/**
 *
 */
public abstract class MemberInvoker {

    /** The descriptor of the method. */
    private final Member member;

    /** The method handle for the method. */
    protected final MethodHandle methodHandle;

    protected final VarHandle varHandle;

    final boolean isVolatileField;

    protected MemberInvoker(Member member, Lookup lookup) {
        this.member = member;
        if (member instanceof InternalMethodDescriptor) {
            InternalMethodDescriptor descriptor = (InternalMethodDescriptor) member;
            try {
                this.methodHandle = descriptor.unreflect(lookup);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessRuntimeException("method " + descriptor + " is not accessible for lookup object " + lookup, e);
            }
            this.varHandle = null;
            this.isVolatileField = false;
        } else {
            InternalFieldDescriptor descriptor = (InternalFieldDescriptor) member;
            try {
                this.varHandle = descriptor.unreflect(lookup);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessRuntimeException("Field " + descriptor + " is not accessible for lookup object " + lookup, e);
            }
            this.methodHandle = null;
            this.isVolatileField = Modifier.isVolatile(descriptor.getModifiers());
        }
    }

    /**
     * Returns the descriptor of the method.
     * 
     * @return the descriptor of the method
     */
    public final Member member() {
        return member;
    }

    protected final void set(Object instance, Object value) {

    }
}
