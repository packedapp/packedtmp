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

import java.lang.invoke.MethodHandles;

import app.packed.assembly.Assembly;
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
public interface ContainerTransformer {

    /**
     * Invoked immediately after a new container is created.
     * <p>
     * For assemblies with multiple processors. The processors for this method will be invoked in the reverse order of
     * {@link #beforeBuild(ContainerConfiguration)}.
     * <p>
     * If {@link Assembly#build()} throws an exception this method will not be invoked.
     *
     * @param configuration
     *            the configuration of the container
     */

    // I think base extension is the only thing that is installed

    // What if an extension installs new extensions from onNew?? I guess, we just need to line up
    default void onNew(ContainerConfiguration configuration) {}

    // BaseExtension is not called here?
    // We take Extension. Because you can always just call configuration.use(extensionClass)? if had an extensionClass as a
    // parameter instead
    default void onExtensionAdded(Extension<?> extension) {}

    /**
     * Invoked immediately before the runtime calls {@link Assembly#build()}.
     *
     * @param configuration
     *            the configuration of the container
     */
    default void onAssemblyClose(ContainerConfiguration configuration) {}

    // Don't know what I can do here
    default void onApplicationClose(ContainerConfiguration configuration) {}

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
    default void verify(ContainerMirror mirror) {}

    default Assembly transformRecursively(MethodHandles.Lookup caller, Assembly assembly) {
        return assembly;
    }
}