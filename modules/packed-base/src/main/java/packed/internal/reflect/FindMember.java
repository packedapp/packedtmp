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

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Set;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.base.reflect.ParameterDescriptor;
import app.packed.inject.InjectionContext;
import app.packed.inject.UnresolvedDependencyException;
import packed.internal.inject.PackedInjectionContext;
import packed.internal.inject.ServiceDependency;
import packed.internal.reflect.FunctionResolver.Entry;
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
class FindMember {

    final MethodType expected;

    final MethodHandle executable;
    final List<Parameter> parameters;
    final int add;
    final int[] permutationArray;

    FindMember(OpenClass oc, Executable e, FunctionResolver aa) {
        expected = aa.callSiteType();

        boolean isInstanceMethod = false;

        // Setup MethodHandle for constructor or method
        if (e instanceof Constructor) {
            executable = oc.unreflectConstructor((Constructor<?>) e, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
        } else {
            Method m = (Method) e;
            executable = oc.unreflect(m, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
            isInstanceMethod = !Modifier.isStatic(m.getModifiers());

            if (isInstanceMethod) {
                if (m.getDeclaringClass() != aa.callSiteType().parameterType(0)) {
                    throw new IllegalArgumentException(
                            "First signature parameter type must be " + m.getDeclaringClass() + " was " + aa.callSiteType().parameterType(0));
                }
            }
        }

        parameters = List.of(e.getParameters());

        add = isInstanceMethod ? 1 : 0;
        permutationArray = new int[parameters.size() + add];
        if (isInstanceMethod) {
            permutationArray[0] = 0;
        }
    }

    MethodHandle find(OpenClass oc, Executable e, FunctionResolver aa) {
        final MethodType expected = aa.callSiteType();

        boolean isInstanceMethod = false;

        // Setup MethodHandle for constructor or method
        MethodHandle mh;
        if (e instanceof Constructor) {
            mh = oc.unreflectConstructor((Constructor<?>) e, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
        } else {
            Method m = (Method) e;
            mh = oc.unreflect(m, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
            isInstanceMethod = !Modifier.isStatic(m.getModifiers());

            if (isInstanceMethod) {
                if (m.getDeclaringClass() != aa.callSiteType().parameterType(0)) {
                    throw new IllegalArgumentException(
                            "First signature parameter type must be " + m.getDeclaringClass() + " was " + aa.callSiteType().parameterType(0));
                }
            }
        }

        List<Parameter> parameters = List.of(e.getParameters());

        int add = isInstanceMethod ? 1 : 0;
        int[] permutationArray = new int[parameters.size() + add];
        if (isInstanceMethod) {
            permutationArray[0] = 0;
        }

        int injectionContext = -1;
        // Nej vi binder nogen til constant... Saa det er ikke sikkert...

        for (int i = 0; i < parameters.size(); i++) {
            Parameter p = parameters.get(i);
            int index;
            if (p.getType() == InjectionContext.class) {
                index = injectionContext = expected.parameterCount();
            } else {
                ServiceDependency sd = ServiceDependency.fromVariable(ParameterDescriptor.from(p));
                Class<?> raw = sd.key().typeLiteral().rawType();

                FunctionResolver.AnnoClassEntry anno = find(aa, p);

                MethodHandle collectMe = null;
                if (anno == null) {
                    Key<?> kk = Key.of(p.getType());
                    Entry entry = aa.keys.get(kk);
                    if (entry != null) {
                        index = entry.indexes[0];
                        if (entry.transformer != null) {
                            collectMe = entry.transformer;
                        }
                    } else {
                        throw new UnresolvedDependencyException("" + kk + " Available keys = " + aa.keys.keySet());
                    }
                } else {
                    MethodHandle tmp = MethodHandles.insertArguments(anno.mh, 1, raw);
                    tmp = MethodHandles.explicitCastArguments(tmp, MethodType.methodType(raw, tmp.type().parameterArray()[0]));
                    System.out.println("----");
                    System.out.println(mh.type());
                    System.out.println(tmp.type());
                    System.out.println(i + " " + anno.index);
                    index = anno.index;
                    collectMe = tmp;
                }
                if (collectMe != null) {
                    if (sd.isOptional()) {
                        // Need to cast return type of collect to Object in order to feed it to Optional.ofNullable(Object)
                        collectMe = MethodHandles.explicitCastArguments(collectMe, collectMe.type().changeReturnType(Object.class));
                        collectMe = MethodHandles.collectArguments(FindMemberHelper.OPTIONAL_OF_NULLABLE, 0, collectMe);
                    }
                    mh = MethodHandles.collectArguments(mh, i + add, collectMe);
                }
            }
            permutationArray[i + add] = index;
        }

        if (injectionContext != -1) {
            MethodType e2 = expected.appendParameterTypes(InjectionContext.class);
            mh = MethodHandles.permuteArguments(mh, e2, permutationArray);
            PackedInjectionContext pic = new PackedInjectionContext(e.getDeclaringClass(), Set.copyOf(aa.keys.keySet()));
            mh = MethodHandles.insertArguments(mh, injectionContext, pic);
        } else {
            mh = MethodHandles.permuteArguments(mh, expected, permutationArray);
        }

        return mh;
    }

    @Nullable
    private FunctionResolver.AnnoClassEntry find(FunctionResolver aa, Parameter p) {
        for (Annotation a : p.getAnnotations()) {
            if (aa.annoations.containsKey(a.annotationType())) {
                return aa.annoations.get(a.annotationType());
            }
        }
        return null;
    }
}
