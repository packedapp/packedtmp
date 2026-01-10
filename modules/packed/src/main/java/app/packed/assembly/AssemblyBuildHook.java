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

import java.util.List;

import app.packed.build.BuildException;
import app.packed.build.hook.ApplyBuildHook;
import app.packed.build.hook.BuildHook;
import app.packed.util.AnnotationList;

/**
 * A build hook that can be applied to an {@link Assembly} class using the {@link ApplyBuildHook} annotation.
 * <p>
 * It is also possible to dynamically apply an assembly hook to one or more assemblies using
 */
public non-sealed abstract class AssemblyBuildHook extends BuildHook {

    /**
     * Invoked immediately after the runtime calls {@link BuildableAssembly#build()}.
     *
     * @param configuration
     *            the configuration of the container
     */
    // We need to add info on the assembly here, such as assembly class
    public void afterBuild(AssemblyConfiguration configuration) {}

    /**
     * Invoked immediately before the runtime calls {@link BuildableAssembly#build()}.
     *
     * @param configuration
     *            the configuration of the container
     */
    public void beforeBuild(AssemblyConfiguration configuration) {}

    // Kørt førend noget andet
    /**
     * Before any build hooks is applied to the assembly. This method is run to reorder/intr.
     * <p>
     * Den her ændre rækkefølgen
     * <p>
     * It is important to note that changing
     *
     * @param assemblyClass
     * @param annotations
     * @return
     */
    // Would it be nice to now previous transformations on the assembly??
    // Hvad sker der med den order vi køre den her metode i..
    // Maaske, kan man ikke aendre transformBuildHooks orderen

    // Maaske vi skal have to here... En der kun tager assembly hooks? og en der tager alle
    // Skal arbejde lidt med Declared/Applied

    public List<BuildHook> transformDeclaredBuildHooks(AssemblyDescriptor descriptor, List<BuildHook> buildHooks) {
        return buildHooks;
    }

    public List<AssemblyBuildHook> transformAppliedBuildHooks(AssemblyDescriptor descriptor, List<AssemblyBuildHook> buildHooks) {
        return buildHooks;
    }

    /**
     * Transforms the annotations on an assembly.
     * <p>
     * This specified annotation list will never contain any {@link ApplyBuildHook} annotations as these cannot be
     * transformed at this point. If you wish to reorder build hooks use
     * {@link #transformBuildHooks(AssemblyDescriptor, List)}.
     *
     * @param assemblyClass
     * @param annotations
     * @return
     */
    // Maybe build hooks are not included in the annotation list???
    // I think better thing is that we fail on modifications
    public AnnotationList transformAssemblyAnnotations(AssemblyDescriptor descriptor, AnnotationList annotations) {
        return annotations;
    }

    /**
     * When an application has been built successfully. This method will be called for each assembly where this build hook
     * is applied.
     * <p>
     *
     * @param mirror
     *            a mirror of the assembly to verify
     */
    // Do we take a ApplicationVerify thingy where we can register issues??? IDK
    public void verify(AssemblyMirror assembly) {}

}
//// Ikke super brugbar, maaske ikke static
//public static AssemblyBuildHook runIf(ApplyBuildHook hook, Predicate<? super AssemblyDescriptor> pred) {
//  throw new UnsupportedOperationException();
//}

@ApplyBuildHook(hooks = MyCompanyAssembly.Max1Container.class)
abstract class MyCompanyAssembly extends BaseAssembly {

    static class Max1Container extends AssemblyBuildHook {

        @Override
        public void verify(AssemblyMirror assembly) {
            if (assembly.containers().count() > 1) {
                throw new BuildException("OOPS"); // throw new BadDeveloperException("");
            }
        }
    }
}
