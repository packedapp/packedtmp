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
import java.util.Optional;
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

//We want to create a ConstructorFinder instance that we reuse..
//So lookup object is probably an optional argument
//The rest is static, its not for injection, because we need
class FindMember {

    final MethodType input;

    final MethodHandle executable;

    final List<Parameter> parameters;
    final int add;
    final int[] permutationArray;
    final FunctionResolver aa;

    final Class<?> declaringClass;

    FindMember(OpenClass oc, Executable e, FunctionResolver aa) {
        this.aa = aa;
        input = aa.callSiteType();

        boolean isInstanceMethod = false;

        // Setup MethodHandle for constructor or method
        if (e instanceof Constructor) {
            executable = oc.unreflectConstructor((Constructor<?>) e, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
        } else {
            Method m = (Method) e;
            executable = oc.unreflect(m, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
            isInstanceMethod = !Modifier.isStatic(m.getModifiers());

            // If Instance method callsite type must always have the receiver at index 0
            if (isInstanceMethod) {
                if (m.getDeclaringClass() != aa.callSiteType().parameterType(0)) {
                    throw new IllegalArgumentException(
                            "First signature parameter type must be " + m.getDeclaringClass() + " was " + aa.callSiteType().parameterType(0));
                }
            }
        }
        this.declaringClass = e.getDeclaringClass();
        parameters = List.of(e.getParameters());

        add = isInstanceMethod ? 1 : 0;
        permutationArray = new int[parameters.size() + add];
        if (isInstanceMethod) {
            permutationArray[0] = 0;
        }
    }

    enum Transformer {
        NONE, OPTIONAL, PROVIDER, COMPOSITE;
    }

    MethodHandle find() {
        MethodHandle mh = executable;
        IntStack is = new IntStack();

        // vi pusher den rigtig vaerdi vi skal bruge for alle af dem
        // dvs is.push(index)
        // Er der andre en InjectionContext der laver nyt?
        // Taenker jeg ikke

        for (int i = 0; i < parameters.size(); i++) {
            Parameter p = parameters.get(i);
            ServiceDependency sd = ServiceDependency.fromVariable(ParameterDescriptor.from(p));
            Class<?> askingForType = sd.key().typeLiteral().rawType();
            FunctionResolver.AnnoClassEntry anno = find(aa, p);

            if (anno == null) {
                Key<?> kk = sd.key();

                // Injection Context
                if (kk.equals(Key.of(InjectionContext.class))) {
                    // TODO we have a non-constant injection context, when we have a dynamic injector

                    // Vi just add it as a normal entry with no indexes, will be picked up in the next section
                    PackedInjectionContext pic = new PackedInjectionContext(declaringClass, Set.copyOf(aa.keys.keySet()));
                    aa.keys.putIfAbsent(kk, new Entry(new int[0], MethodHandles.constant(InjectionContext.class, pic)));
                }

                Entry entry = aa.keys.get(kk);
                if (entry != null) {
                    // Vi have an explicit registered service.

                    if (entry.transformer != null) {
                        MethodHandle transformer = entry.transformer;
                        if (sd.isOptional()) {
                            // We need to the return value of transformer to an optional
                            transformer = MethodHandles.filterReturnValue(transformer, FindMemberHelper.optionalOfTo(askingForType));
                        }
                        mh = MethodHandles.collectArguments(mh, is.size() + add, transformer);
                    } else {
                        // We use a provided value directly. Wrap it in an Optional if needed
//                        Class<?> expectedArg = executable.type().parameterType(entry.indexes[0]);
//                        System.out.println("EXPECTED ______________" + expectedArg);
//                        System.out.println("ACTUAL ______________" + kk);
                        if (sd.isOptional()) {
                            mh = MethodHandles.filterArguments(mh, is.size() + add, FindMemberHelper.optionalOfTo(askingForType));
                        }
                    }
                    is.push(entry.indexes);
                } else {
                    // Det er saa her, at vi kalder ind paa en virtuel injector...
                    if (sd.isOptional()) {
                        mh = MethodHandles.insertArguments(mh, is.size() + add, Optional.empty());
                    } else {
                        // Could be inner class
                        throw new UnresolvedDependencyException("Could not inject " + kk + " Available keys = " + aa.keys.keySet());
                    }
                }
            } else {
                // Annnotation
                // Vi supportere kun lige nu noget der sporger paa typen (class)
                // Som vi binder til en supplied methodhandle som vi derefter kalder
                MethodHandle tmp = MethodHandles.insertArguments(anno.mh, 1, askingForType);
                tmp = MethodHandles.explicitCastArguments(tmp, MethodType.methodType(askingForType, tmp.type().parameterArray()[0]));
                // System.out.println(mh.type());
                if (sd.isOptional()) {
                    // We need to the return value of transformer to an optional, may be null
                    tmp = MethodHandles.filterReturnValue(tmp, FindMemberHelper.optionalOfNullableTo(askingForType));
                }

                mh = MethodHandles.filterArguments(mh, is.size() + add, tmp);
                is.push(anno.index);
            }
        }

        // Providere.. Der binder man en MethodHandle

        if (add == 1) {
            mh = MethodHandles.permuteArguments(mh, input, is.toArrayAdd0());
        } else {
            mh = MethodHandles.permuteArguments(mh, input, is.toArray());
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
//
//MethodHandle findOld() {
//    MethodHandle mh = executable;
//    int injectionContext = -1;
//
//    for (int i = 0; i < parameters.size(); i++) {
//        Parameter p = parameters.get(i);
//        ServiceDependency sd = ServiceDependency.fromVariable(ParameterDescriptor.from(p));
//        Class<?> askingForType = sd.key().typeLiteral().rawType();
//        int index;
//        MethodHandle collectMe = null;
//
//        if (askingForType == InjectionContext.class) {
//            index = injectionContext = input.parameterCount();
//        } else {
//            FunctionResolver.AnnoClassEntry anno = find(aa, p);
//
//            if (anno == null) {
//                Key<?> kk = Key.of(p.getType());
//                Entry entry = aa.keys.get(kk);
//                if (entry != null) {
//                    index = entry.indexes[0];
//                    if (entry.transformer != null) {
//                        collectMe = entry.transformer;
//                    }
//                    // Else it just the argument being relayed directly
//                } else {
//                    // Vil gerne indsaette argument...
//                    throw new UnresolvedDependencyException("" + kk + " Available keys = " + aa.keys.keySet());
//                }
//            } else {
//                MethodHandle tmp = MethodHandles.insertArguments(anno.mh, 1, askingForType);
//                tmp = MethodHandles.explicitCastArguments(tmp, MethodType.methodType(askingForType, tmp.type().parameterArray()[0]));
//                index = anno.index;
//                collectMe = tmp;
//            }
//
//        }
//        if (sd.isOptional()) {
//            if (collectMe != null) {
//                // Need to cast return type of collect to Object in order to feed it to Optional.ofNullable(Object)
//                collectMe = MethodHandles.explicitCastArguments(collectMe, collectMe.type().changeReturnType(Object.class));
//                collectMe = MethodHandles.collectArguments(FindMemberHelper.OPTIONAL_OF_NULLABLE, 0, collectMe);
//            } else {
//                // Men den eksistere jo ikke endnu...
//                mh = MethodHandles.collectArguments(mh, index, FindMemberHelper.OPTIONAL_OF_NULLABLE);
//                // collectMe = MethodHandles.collectArguments(FindMemberHelper.OPTIONAL_OF_NULLABLE, i + add, mh);
//            }
//        }
//
//        if (collectMe != null) {
//            mh = MethodHandles.collectArguments(mh, i + add, collectMe);
//        }
//        permutationArray[i + add] = index;
//    }
//
//    if (injectionContext != -1) {
//        // Vi indsaetter den som et argument fordi, saa kan runtimen selv finde ud af genbruge den...
//        // Hvis flere argumenter efterspoerger den
//        MethodType e2 = input.appendParameterTypes(InjectionContext.class);
//        mh = MethodHandles.permuteArguments(mh, e2, permutationArray);
//
//        // En senere implementation har jo faktisk brug for baade aa.keys.ketSet() og noget runtime context...
//        // Saa vi bliver vel noedt til at lave et partial object vi kan injecte. Hvorefter den saa bliver lavet
//        // med en MH
//        PackedInjectionContext pic = new PackedInjectionContext(declaringClass, Set.copyOf(aa.keys.keySet()));
//        mh = MethodHandles.insertArguments(mh, injectionContext, pic);
//    } else {
//        System.out.println("Before permute " + mh.type());
//        mh = MethodHandles.permuteArguments(mh, input, permutationArray);
//        System.out.println("After permute " + mh.type());
//    }
//
//    return mh;
//}