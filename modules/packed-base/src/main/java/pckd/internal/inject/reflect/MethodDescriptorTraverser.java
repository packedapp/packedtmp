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

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.util.MethodDescriptor;

/**
 *
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

    public static final MethodDescriptorTraverser DEEP = null;

    /**
     * Returns whether or not overridden methods should be ignored. The value is {@code true}.
     * 
     * @return whether or not overridden methods should be ignored
     */
    protected boolean ignoreOverriddenMethods() {
        return true;
    }

    public final void forEach(Class<?> targetClass, Consumer<? super MethodDescriptor> consumer) {

    }

    public final Stream<MethodDescriptor> stream(Class<?> targetClass) {
        throw new UnsupportedOperationException();
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
        DEEP.stream(List.class).forEach(e -> System.out.println(e));
        DEEP.forEach(List.class, e -> System.out.println(e));

        for (MethodDescriptor d : DEEP.toList(List.class)) {
            System.out.println(d);
        }
    }
}
