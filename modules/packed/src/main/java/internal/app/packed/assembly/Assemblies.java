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
package internal.app.packed.assembly;

/**
 * Various utility methods for {@link Assembly assemblies}.
 */
// This is going away for BuildHook.apply
// We really want to use a lambda to capture who initiated it
public final class Assemblies {

    private Assemblies() {}

//    static void main(Assembly a) {
//        Assemblies.verifyRecursively(a, ContainerMirror.class, t -> System.out.println(t.componentPath() + " Number of beans = " + t.beans().count()));
//        DelegatingAssembly.applyBuildHook(a, s -> s.traverseRecursively().verify(ContainerMirror.class, t -> System.out.println(t.componentPath() + " Number of beans = " + t.beans().count())));
//    }
//
//    public static Assembly observe(Assembly assembly, AssemblyPropagator ap, BuildHook... hooks) {
//        return wireWith(assembly, wireletObserve(ap, hooks));
//    }
//
//    public static Assembly observe(Assembly assembly, BuildHook... hooks) {
//        return wireWith(assembly, wireletObserve(AssemblyPropagator.LOCAL, hooks));
//    }
//
//    public static Assembly observeRecursively(Assembly assembly, BuildHook... hooks) {
//        return wireWith(assembly, wireletObserve(AssemblyPropagator.ALL, hooks));
//    }
//
//    // TODO could also be a lambda that does stuff.... And we capture the module from the lambda...
//    public static Assembly transform(MethodHandles.Lookup caller, Assembly assembly, AssemblyPropagator ap, BuildHook... hooks) {
//        return assembly;
//    }
//
//    /**
//     * @param caller
//     * @param assembly
//     * @return
//     * @throws IllegalArgumentException
//     *             if the specified assembly is in use or has already been used
//     */
//    // Writes
//    // We don't actually transform anything, just returns a new assembly that will do it
//    // For assemblies, we do not need the lookup caller
//
//    // c-> c.applyRecursively(fooHook)
//
//    public static Assembly transform(MethodHandles.Lookup caller, Assembly assembly, BuildHook... hooks) {
//        return assembly;
//    }
//
//    // Read only
//    public static Assembly transformRecursively(MethodHandles.Lookup caller, Assembly assembly, BuildHook... hooks) {
//        return assembly;
//    }
//
//    public static <T extends ComponentMirror> Assembly verify(Assembly assembly, AssemblyPropagator ap, Class<T> mirrorType, Consumer<? super T> verifier) {
//        return assembly;
//    }
//
//    // I think if you want them parallel, you probably need to write them yourself.
//    public static <T extends ComponentMirror> Assembly verify(Assembly assembly, Class<T> mirrorType, Consumer<? super T> verifier) {
//        return verify(assembly, AssemblyPropagator.LOCAL, mirrorType, verifier);
//    }
//
//    public static <T extends ComponentMirror> Assembly verifyRecursively(Assembly assembly, Class<T> mirrorType, Consumer<? super T> verifier) {
//        return verify(assembly, AssemblyPropagator.ALL, mirrorType, verifier);
//    }

    // I don't think we support wirelets...
    // Any extension can just sneak them in and inspect the application

    // Maybe they are simply a methods on Wirelets once we agree on the syntax
//    public static Wirelet wireletObserve(AssemblyPropagator ap, BuildHook... hooks) {
//        throw new UnsupportedOperationException();
//    }
//
//    public static Wirelet wireletObserve(BuildHook... hooks) {
//        return wireletObserve(AssemblyPropagator.LOCAL, hooks);
//    }
//
//    public static Wirelet wireletObserveRecursively(BuildHook... hooks) {
//        return wireletObserve(AssemblyPropagator.ALL, hooks);
//    }

}
// This could just be a wirelet...If it is readable...
// Wirelet.verify(Class<? extends T> mirrorType, Consumer<T> verifier) <--- Applies to the whole Assembly
// Wirelet.verify(Class<? extends T> mirrorType, Consumer<T> verifier, AssemblyPropagator ap)
//public static Assembly observe(Assembly assembly, BuildObserver... observers) {
//    return assembly;
//}
