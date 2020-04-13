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
package packed.internal.reflect;

import static packed.internal.util.StringFormatter.format;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import app.packed.base.InvalidDeclarationException;
import app.packed.base.Key;
import app.packed.inject.Inject;
import app.packed.inject.InjectionContext;
import app.packed.inject.UnresolvedDependencyException;
import packed.internal.reflect.InjectionSpec.Entry;
import packed.internal.util.StringFormatter;
import packed.internal.util.UncheckedThrowableFactory;

/**
 *
 */
//Allow multiple constructors, For example take a list of MethodType...
//Custom ExtensionTypes

//Maybe a little customization of error messages...
//Maybe just a protected method that creates the message that can then be overridden.

//Usage
//Bundle <- No Args, with potential custom lookup object
//OnHookBuilder <- No args
//Extension <- Maybe PackedExtensionContext
//ExtensionComposer <- No Arg

//We want to create a ConstructorFinder instance that we reuse..
//So lookup object is probably an optional argument
//The rest is static, its not for injection, because we need

//So ConstructorFinder is probably a bad name..
public class FindConstructor {

    ArrayList<Parameter> parameters;

    public MethodHandle doIt(OpenClass oc, InjectionSpec aa) {
        Constructor<?> constructor = findInjectableConstructor(aa.input().returnType());
        return doIt(oc, constructor, aa);
    }

    public MethodHandle doIt(OpenClass oc, Executable e, InjectionSpec aa) {

        MethodType expected = aa.input();

        boolean isInstanceMethod = false;
        MethodHandle mh;
        if (e instanceof Constructor) {
            mh = oc.unreflectConstructor((Constructor<?>) e, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
        } else {
            Method m = (Method) e;
            mh = oc.unreflect(m, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
            isInstanceMethod = !Modifier.isStatic(m.getModifiers());
        }
        // MethodHandle mh = oc.unreflectConstructor(constructor,
        // UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
        this.parameters = new ArrayList<>(List.of(e.getParameters()));

        int injectionContext = -1;
        // Nej vi binder nogen til constant... Saa det er ikke sikkert...

        int add = isInstanceMethod ? 1 : 0;
        int[] permutationArray = new int[this.parameters.size() + add];
        if (isInstanceMethod) {
            permutationArray[0] = 0;
        }
        for (int i = 0; i < this.parameters.size(); i++) {
            Parameter p = parameters.get(i);
            int index;
            if (p.getType() == InjectionContext.class) {
                index = injectionContext = expected.parameterCount();
            } else {
                Key<?> kk = Key.of(p.getType());
                Entry entry = aa.keys.get(kk);
                if (entry != null) {
                    index = entry.index;
                    if (entry.transformer != null) {
                        mh = MethodHandles.collectArguments(mh, i, entry.transformer);
                    }
                } else {
                    throw new UnresolvedDependencyException("" + kk + " Available keys = " + aa.keys.keySet());
                }
            }
            permutationArray[i + add] = index;
        }

        if (injectionContext != -1) {
            MethodType e2 = expected.appendParameterTypes(InjectionContext.class);
            mh = MethodHandles.permuteArguments(mh, e2, permutationArray);
            PackedInjectionContext pic = new PackedInjectionContext(expected.returnType(), Set.copyOf(aa.keys.keySet()));
            mh = MethodHandles.insertArguments(mh, injectionContext, pic);

        } else {
            mh = MethodHandles.permuteArguments(mh, expected, permutationArray);
        }

        return mh;
    }

    Constructor<?> findInjectableConstructor(Class<?> type) {
        if (type.isArray()) {
            throw new IllegalArgumentException(format(type) + " is an array and cannot be instantiated");
        } else if (type.isAnnotation()) {
            throw new IllegalArgumentException(format(type) + ") is an annotation and cannot be instantiated");
        } else if (Modifier.isAbstract(type.getModifiers())) {
            throw new IllegalArgumentException("'" + StringFormatter.format(type) + "' cannot be an abstract class");
        }
//        if (Modifier.isAbstract(onType.getModifiers())) {
//            throw tf.newThrowable("'" + StringFormatter.format(onType) + "' cannot be an abstract class");
//        } else if (TypeUtil.isInnerOrLocalClass(onType)) {
//            throw tf.newThrowable("'" + StringFormatter.format(onType) + "' cannot be an inner or local class");
//        }
        Constructor<?>[] constructors = type.getDeclaredConstructors();

        // If we only have 1 constructor, return it.
        if (constructors.length == 1) {
            return constructors[0];
        }

        // See if we have a single constructor annotated with @Inject
        Constructor<?> constructor = null;
        for (Constructor<?> c : constructors) {
            if (c.isAnnotationPresent(Inject.class)) {
                if (constructor != null) {
                    throw new InvalidDeclarationException(
                            "Multiple constructors annotated with @" + Inject.class.getSimpleName() + " on class " + format(type));
                }
                constructor = c;
            }
        }
        if (constructor != null) {
            return constructor;
        }

        for (Constructor<?> c : constructors) {
            if (Modifier.isPublic(c.getModifiers())) {
                if (constructor != null) {
                    throw new IllegalArgumentException(
                            "No constructor annotated with @" + Inject.class.getSimpleName() + ". And multiple public constructors on class " + format(type));
                }
                constructor = c;
            }
        }
        if (constructor != null) {
            return constructor;
        }

        for (Constructor<?> c : constructors) {
            if (Modifier.isProtected(c.getModifiers())) {
                if (constructor != null) {
                    throw new IllegalArgumentException("No constructor annotated with @" + Inject.class.getSimpleName()
                            + ". And multiple protected constructors on class " + format(type));
                }
                constructor = c;
            }
        }
        if (constructor != null) {
            return constructor;
        }

        // Remaining constructors are private or package private
        for (Constructor<?> c : constructors) {
            if (!Modifier.isPrivate(c.getModifiers())) {
                if (constructor != null) {
                    throw new IllegalArgumentException("No constructor annotated with @" + Inject.class.getSimpleName()
                            + ". And multiple package-private constructors on class " + format(type));
                }
                constructor = c;
            }
        }
        if (constructor != null) {
            return constructor;
        }

        // Only private constructors left, and we have already checked whether or not we only have a single method
        // So we must have more than 1 private methods
        throw new IllegalArgumentException(
                "No constructor annotated with @" + Inject.class.getSimpleName() + ". And multiple private constructors on class " + format(type));
    }
}
