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
package app.packed.container;

import app.packed.build.hook.BuildHook;
import app.packed.extension.Extension;

/**
 * An assembly hook is super cool
 *
 * <p>
 * For the methods on this interface taking a {@link ContainerConfiguration} the following applies:
 *
 * The realm of the container configuration will be this class. Any value specified to
 * {@link Assembly#lookup(MethodHandles.Lookup)} will be reset before next context or the actual build method
 */
// This is user facing. (Not for extensions)
// What about AssemblyTransformer.preBuildt vs ContainerTransformer.onNew???
// I think this means we need to call these other transformers before other prebuilts...

public non-sealed abstract class ContainerBuildHook extends BuildHook {

    // Don't know what I can do here
    public void onApplicationClose(ContainerConfiguration configuration) {}

    /**
     * Invoked immediately before the runtime calls {@link Assembly#build()}.
     *
     * @param configuration
     *            the configuration of the container
     */
    public void onAssemblyClose(ContainerConfiguration configuration) {}

    /**
     * Invoked by the framework when a new extension is added to the container.
     * <p>
     * This method is never invoked for {@link app.packed.extension.BaseExtension}, as this extension is always present.
     *
     * @param extension
     *            the extension that was added
     */
    public void onExtensionAdded(ContainerConfiguration configuration, Extension<?> extension) {}

    /**
     * Invoked immediately after a new container is created.
     * <p>
     * In general it is bad practice to install new beans from this method. As the assembly itself might want to set up some
     * stuff
     *
     * <p>
     * For assemblies with multiple processors. The processors for this method will be invoked in the reverse order of
     * {@link #beforeBuild(ContainerConfiguration)}.
     * <p>
     * If {@link Assembly#build()} throws an exception this method will not be invoked.
     *
     * @param configuration
     *            the configuration of the container
     */
    // What if an extension installs new extensions from onNew?? I guess, we just need to line up
    // Nah I think
    public void onNew(ContainerConfiguration configuration) {}

//    public Assembly transformRecursively(MethodHandles.Lookup caller, Assembly assembly) {
//        return assembly;
//    }

    /**
     * When an application has finished building this method is called to check.
     * <p>
     *
     * on because it should be a notification thingy, or should we reserve on to Async
     *
     * onSuccess??? verify?
     *
     * @param mirror
     *            a mirror of the assembly to verify
     *
     * @see AssemblyMirror#containers()
     */
    // ? T
    // Do we take a ApplicationVerify thingy where we can register issues??? IDK
    // ContainerMirror
    // I think this a generic thingy...
    // We shouldn't need
    // predicateClass == Predicate<? extends Consumer<ContainerMirror>>>

    // Man kunne ogsaa simpelthen skrive det til en fil her...
    public void verify(ContainerMirror mirror) {}

    // Doesn't really work... We need to aggreate them
//
//    static Assembly interceptOnNew(Assembly assembly, AssemblyPropagator ap, Consumer<? super ContainerConfiguration> c) {
//        return assembly;
//    }
//
//    // Will create a fake BuildTransformer
//    // Super cool. But I think it is really having something that implement BuildTransformer
//    // At least for the mirrors
//    static Assembly interceptOnNew(Assembly assembly, Consumer<? super ContainerConfiguration> c) {
//        return interceptOnNew(assembly, AssemblyPropagator.LOCAL, c);
//    }
//
//    static Assembly transformRecursively(Supplier<? extends BuildHook> c, Assembly assembly) {
//
//        // Must report this line as the build transformer source...
//        interceptOnNew(assembly, f -> f.named("sdf"));
//
//        return assembly;
//    }
}

// Whenever an extension of the specified type is installed, do X
interface ExtensionHook<E extends Extension<E>> {

}