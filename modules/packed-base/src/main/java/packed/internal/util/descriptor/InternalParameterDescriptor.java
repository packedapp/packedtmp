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
package packed.internal.util.descriptor;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import app.packed.inject.Dependency;
import app.packed.inject.TypeLiteral;
import app.packed.util.ExecutableDescriptor;
import app.packed.util.Nullable;
import app.packed.util.ParameterDescriptor;
import packed.internal.util.InternalErrorException;

/** The default implementation of {@link ParameterDescriptor}. */
public final class InternalParameterDescriptor extends AbstractVariableDescriptor implements ParameterDescriptor {

    /** The executable that declares the parameter. */
    private final AbstractExecutableDescriptor declaringExecutable;

    /** The index of the parameter. */
    private final int index;

    /** The actual parameter this instance is an descriptor for. */
    private final Parameter parameter;

    /**
     * Creates a new descriptor
     *
     * @param declaringExecutable
     *            the executable that declares the parameter
     * @param parameter
     *            the parameter
     * @param index
     *            the index of the parameter
     */
    public InternalParameterDescriptor(AbstractExecutableDescriptor declaringExecutable, Parameter parameter, int index) {
        super(parameter);
        this.declaringExecutable = declaringExecutable;
        this.parameter = parameter;
        this.index = index;
    }

    /** {@inheritDoc} */
    @Override
    public String descriptorTypeName() {
        return "parameter";
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof InternalParameterDescriptor) {
            return ((InternalParameterDescriptor) obj).parameter.equals(parameter);
        } else if (obj instanceof ParameterDescriptor) {
            return ((ParameterDescriptor) obj).newParameter().equals(parameter);
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getDeclaringClass() {
        return parameter.getDeclaringExecutable().getDeclaringClass();
    }

    /** {@inheritDoc} */
    @Override
    public ExecutableDescriptor getDeclaringExecutable() {
        return declaringExecutable;
    }

    /** {@inheritDoc} */
    @Override
    public int getIndex() {
        return index;
    }

    /** {@inheritDoc} */
    @Override
    public int getModifiers() {
        return parameter.getModifiers();
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return parameter.getName();
    }

    @Override
    public Type getParameterizedType() {
        Class<?> dc = getDeclaringClass();
        // Works around for https://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8213278
        if (dc.isLocalClass() || (dc.isMemberClass() && !Modifier.isStatic(dc.getModifiers()))) {
            return declaringExecutable.executable.getGenericParameterTypes()[getIndex() - 1];
        } else {
            return parameter.getParameterizedType();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getType() {
        return parameter.getType();
    }

    /** {@inheritDoc} */
    @Override
    public TypeLiteral<?> getTypeLiteral() {
        return TypeLiteral.fromParameter(parameter);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return parameter.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isNamePresent() {
        return parameter.isNamePresent();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPrimitiveType() {
        return parameter.getType().isPrimitive();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isVarArgs() {
        return parameter.isVarArgs();
    }

    /** {@inheritDoc} */
    @Override
    public Parameter newParameter() {
        // Parameter is immutable, but it contains a reference to its declaring executable exposed via
        // Parameter#getDeclaringExecutable. So we need to create a new copy
        return declaringExecutable.newExecutable().getParameters()[index];
    }

    /** {@inheritDoc} */
    @Override
    protected Dependency toDependency0() {
        return Dependency.of(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return parameter.toString();
    }

    /**
     * Creates a new parameter descriptor from the specified parameter.
     *
     * @param parameter
     *            the parameter to create a descriptor for
     * @return a new parameter descriptor
     */
    public static InternalParameterDescriptor of(Parameter parameter) {
        requireNonNull(parameter, "parameter is null");

        AbstractExecutableDescriptor em;
        if (parameter.getDeclaringExecutable() instanceof Constructor) {
            em = InternalConstructorDescriptor.of(parameter.getDeclaringExecutable());
        } else {
            em = InternalMethodDescriptor.of((Method) parameter.getDeclaringExecutable());
        }

        // parameter.index is not visible, so we need to iterate through all parameters to find the right one
        if (em.getParameterCount() == 1) {
            return em.getParameters()[0];
        } else {
            for (InternalParameterDescriptor p : em.getParameters()) {
                if (p.parameter.equals(parameter)) {
                    return p;
                }
            }
        }
        throw new InternalErrorException("parameter", parameter);// We should never get to here
    }

    /**
     * If the specified descriptor is an instance of this class. This method casts and returns the specified descriptor.
     * Otherwise creates a new descriptor.
     *
     * @param descriptor
     *            the descriptor to copy or return
     * @return a parameter descriptor
     */
    public static InternalParameterDescriptor of(ParameterDescriptor descriptor) {
        return descriptor instanceof InternalParameterDescriptor ? (InternalParameterDescriptor) descriptor : of(descriptor.newParameter());
    }

}
