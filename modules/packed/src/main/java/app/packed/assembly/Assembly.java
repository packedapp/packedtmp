/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package app.packed.assembly;

import app.packed.application.ApplicationBuildLocal;
import app.packed.assembly.AbstractComposer.ComposableAssembly;
import app.packed.build.BuildCodeSource;
import org.jspecify.annotations.Nullable;
import internal.app.packed.application.PackedApplicationInstaller;
import internal.app.packed.assembly.AssemblySetup;
import internal.app.packed.container.PackedContainerInstaller;
import internal.app.packed.util.accesshelper.AccessHelper;
import internal.app.packed.util.accesshelper.AssemblyAccessHandler;

/**
 * Assemblies are the basic building block for defining applications in Packed.
 * <p>
 * An assembly provides the concrete instructions for creating an application, and every application is constructed
 * either directly or indirectly from one or more assemblies. As such a single assembly can either comprise the build
 * instructions for an entire application or serve as the root of a hierarchy of assembly nodes, where each node is
 * responsible for building a part of the application.
 * <p>
 * Assemblies provide a simply way to package components and build modular application. This is useful, for example,
 * for:
 * <ul>
 * <li>Sharing functionality across multiple injectors and/or containers.</li>
 * <li>Hiding implementation details from users.</li>
 * <li>Organizing a complex application into distinct sections, such that each section addresses a separate
 * concern.</li>
 * </ul>
 * <p>
 * Assemblies does not have a runtime representation, they are only used at build time.
 * <p>
 * There are currently two types of assemblies available:
 * <ul>
 * <li><b>{@link BaseAssembly}</b> which assemblies information about services, and creates injector instances using
 * .</li>
 * <li><b>{@link BaseAssembly}</b> which assemblies information about both services and components, and creates
 * container instances using .</li>
 * </ul>
 * A assembly instance can be used as part of exactly one ({@link #build()} build-process). Attempting to reuse an
 * assembly will fail with {@link IllegalStateException}.
 * <p>
 * This class cannot be extended directly, you would typically extend {@link BaseAssembly} instead.
 */
@AssemblySecurityPolicy(AssemblySecurityPolicy.Default.class)
public sealed abstract class Assembly implements BuildCodeSource, ApplicationBuildLocal.Accessor
        permits BuildableAssembly, DelegatingAssembly, ComposableAssembly {

    /**
     * Invoked by the runtime (via a MethodHandle) in order to execute the build instructions of the assembly.
     *
     * @param builder
     *            a container builder for the root container of the assembly
     * @return an (internal) assembly configuration object
     *
     * @apiNote this method is for internal use only
     */
    abstract AssemblySetup build(@Nullable PackedApplicationInstaller<?> applicationInstaller, PackedContainerInstaller<?> installer);

    /** The state of an {@link Assembly}. */
    public enum State {

        /** The assembly has already been used in a build process (either successfully or unsuccessfully). */
        AFTER_BUILD,

        /** The assembly has not yet been used in a build process. */
        BEFORE_BUILD,

        /** The assembly is currently being used in a build process. */
        BUILDING;

        public static State of(Assembly assembly) {
            throw new UnsupportedOperationException();
        }
    }

    static {
        AccessHelper.initHandler(AssemblyAccessHandler.class, new AssemblyAccessHandler() {

            @Override
            public AssemblyMirror newAssemblyMirror(AssemblySetup assembly) {
                return new AssemblyMirror(assembly);
            }

            @Override
            public AssemblySetup invokeAssemblyBuild(Assembly assembly, @Nullable PackedApplicationInstaller<?> applicationInstaller,
                    PackedContainerInstaller<?> installer) {
                return assembly.build(applicationInstaller, installer);
            }
        });
    }
}
