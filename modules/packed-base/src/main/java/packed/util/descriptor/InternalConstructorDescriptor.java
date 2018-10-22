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
package packed.util.descriptor;

import static java.util.Objects.requireNonNull;
import static packed.util.Formatter.format;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;

import app.packed.inject.Inject;
import app.packed.util.ConstructorDescriptor;
import packed.inject.JavaXInjectSupport;
import packed.util.InternalErrorException;

/** The default implementation of {@link ConstructorDescriptor}. */
public final class InternalConstructorDescriptor<T> extends AbstractExecutableDescriptor implements ConstructorDescriptor<T> {

    /** The constructor that is being mirrored. */
    private final Constructor<?> constructor;

    /**
     * Creates a new InternalConstructorDescriptor from the specified constructor.
     *
     * @param constructor
     *            the constructor to create a descriptor from
     */
    private InternalConstructorDescriptor(Constructor<?> constructor) {
        super(requireNonNull(constructor, "constructor is null"));
        this.constructor = constructor;
    }

    /** {@inheritDoc} */
    @Override
    public String descriptorName() {
        return "constructor";
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof InternalConstructorDescriptor) {
            return ((InternalConstructorDescriptor<?>) obj).constructor.equals(constructor);
        } else if (obj instanceof ConstructorDescriptor) {
            return ((ConstructorDescriptor<T>) obj).newConstructor().equals(constructor);
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return constructor.getName();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return constructor.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSynthetic() {
        return constructor.isSynthetic();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public Constructor<T> newConstructor() {
        Class<?> declaringClass = constructor.getDeclaringClass();
        try {
            return (Constructor<T>) declaringClass.getConstructor(getParameterTypes());
        } catch (NoSuchMethodException e) {
            throw new InternalErrorException("constructor", constructor, e);// We should never get to here
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Executable newExecutable() {
        return newConstructor();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return format(constructor);
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle unreflect(Lookup lookup) throws IllegalAccessException {
        requireNonNull(lookup, "lookup is null");
        return lookup.unreflectConstructor(constructor);
    }

    /**
     * Creates a new descriptor from the specified constructor.
     *
     * @param constructor
     *            the constructor to wrap
     * @return a new constructor descriptor
     */
    public static <T> InternalConstructorDescriptor<T> of(Constructor<T> constructor) {
        return new InternalConstructorDescriptor<T>(constructor);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> InternalConstructorDescriptor<T> findDefaultForInject(Class<T> clazz) {
        int maxParameters = 0;
        Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();
        InternalConstructorDescriptor<T>[] constructors = new InternalConstructorDescriptor[declaredConstructors.length];
        for (int i = 0; i < declaredConstructors.length; i++) {
            constructors[i] = (InternalConstructorDescriptor<T>) InternalConstructorDescriptor.of(declaredConstructors[i]);
            maxParameters = Math.max(maxParameters, constructors[i].getParameterCount());
        }
        // See if we only have one constructor, in which case we keep it for later
        if (constructors.length == 1) {
            // one = constructors[0];
        }

        // Look for a single constructor annotated with @Inject
        InternalConstructorDescriptor<T> injectable = null;
        for (InternalConstructorDescriptor<T> cm : constructors) {
            if (JavaXInjectSupport.isInjectAnnotationPresent(cm)) {
                if (injectable != null) {
                    throw new IllegalArgumentException("Multiple constructors annotated with @" + Inject.class.getSimpleName() + " on class "
                            + format(constructors[0].getDeclaringClass()));
                }
                injectable = cm;
            }
        }

        // Look for a single constructor with the maximum number of parameters
        if (injectable == null) {
            for (InternalConstructorDescriptor<T> cm : constructors) {
                if (cm.getParameterCount() == maxParameters) {
                    if (injectable != null) {
                        throw new IllegalArgumentException("No constructor annotated with @" + Inject.class.getSimpleName()
                                + ". And multiple constructors having the maximum number of parameters (" + maxParameters + ") on class "
                                + format(constructors[0].getDeclaringClass()));
                    }
                    injectable = cm;
                }
            }
        }
        if (injectable == null) {
            throw new IllegalArgumentException("Did not find anything");
        }
        return injectable;
    }
}
