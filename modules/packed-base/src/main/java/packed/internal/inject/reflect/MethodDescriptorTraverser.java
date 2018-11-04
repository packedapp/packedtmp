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
package packed.internal.inject.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.util.MethodDescriptor;

/**
 * A helper class that can be used to traverse through all method descriptors on a class.
 * Taking into consideration overridden classes and default methods. 
 */
// Filters + Orders

// Det er i virkeligheden lidt som ComponentTraverselOrder

// Ordering Dimension 1
// public int SuperclassFirst = 0;
// public int SuperclassLast = 0;
// public int NoOrdering

// Ordering Dimension 2 -> Comparator<? super MethodDescriptor>

// Ignore Overridden Methods
// include default methods (unreflect should handle default methods on interfaces?)
// Process super classes, process default methods

//Maaske maa vi selv definere filter on me
/**
 * The Java source modifiers that can be applied to a method. @jls8.4.3 Method Modifiers
 */
// private static final int METHOD_MODIFIERS =
// Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE |
// Modifier.ABSTRACT | Modifier.STATIC | Modifier.FINAL |
// Modifier.SYNCHRONIZED | Modifier.NATIVE | Modifier.STRICT;

// Filter on accessors???
// Syntes slet ikke der skal vaere nogle filtre..
public class MethodDescriptorTraverser {

    /** Creates a new processor by subclassing this class. */
    protected MethodDescriptorTraverser() {}

    public static final MethodDescriptorTraverser DEFAULT = new MethodDescriptorTraverser();

    /**
     * Returns whether or not overridden methods should be ignored. The value is {@code true}.
     * 
     * @return whether or not overridden methods should be ignored
     */
    protected boolean ignoreOverriddenMethods() {
        return true;
    }

    public final void forEach(Class<?> targetClass, Consumer<? super MethodDescriptor> action) {
        toList(targetClass).forEach(action);
    }

    public final Stream<MethodDescriptor> stream(Class<?> targetClass) {
        return toList(targetClass).stream();
    }

    /**
     * Returns an immutable list of all method descriptors
     * 
     * @param targetClass
     *            the class
     * @return an immutable list of method descriptors
     */
    public final List<MethodDescriptor> toList(Class<?> targetClass) {
        return null;
    }

    public static void main(String[] args) {
        DEFAULT.stream(List.class).forEach(e -> System.out.println(e));
        DEFAULT.forEach(List.class, e -> System.out.println(e));

        for (MethodDescriptor d : DEFAULT.toList(List.class)) {
            System.out.println(d);
        }
    }
}

/** Actually we do not need this..... */
class MethodSorter implements Comparator<Method> {
    static MethodSorter INSTANCE = new MethodSorter();

    /** {@inheritDoc} */
    @Override
    public int compare(Method m1, Method m2) {
        int val = m1.getName().compareTo(m2.getName());
        if (val == 0) {
            val = m1.getParameterCount() - m2.getParameterCount();
            if (val == 0) {
                Parameter[] p1 = m1.getParameters();
                Parameter[] p2 = m2.getParameters();
                for (int i = 0; i < p2.length; i++) {
                    Class<?> c1 = p1[i].getType();
                    Class<?> c2 = p2[i].getType();
                    if (c1 != c2) {
                        String n1 = c1.getCanonicalName();
                        String n2 = c1.getCanonicalName();
                        // A parameter always have a canonical name
                        return n1.compareTo(n2);
                    }
                }
                throw new InternalError("Should never get here: " + m1 + ", " + m2);
            }
        }
        return val;
    }
}