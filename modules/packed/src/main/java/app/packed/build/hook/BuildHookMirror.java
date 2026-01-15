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
package app.packed.build.hook;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import app.packed.assembly.AssemblyMirror;
import app.packed.assembly.AssemblyPropagator;
import app.packed.build.BuildCodeSourceMirror;
import app.packed.build.Mirror;
import app.packed.component.ComponentMirror;
import app.packed.component.ComponentPath;
import app.packed.util.TreeView;

/**
 * Represents a single instance of a {@link BuildTransformer build transformer}.
 * <p>
 * The transformer is initial declared on a {@link #declaringComponent() component}.
 *
 * The transformer may make use of an {@link AssemblyPropagator propagator}
 */
//
public non-sealed interface BuildHookMirror extends BuildCodeSourceMirror {

    // Where is the hook declared. Annotation on a Class, Wirelet Imperativt, Progamatically
    Object declarationSite(); // WhereAndHow... Wirelet, BuildHook.Foo, Assembly Class, Bean Class

    /** {@return What component was the transformer applied to. For example, ContainerMirror, for a bean} */
    // What if wirelet??? Assembly, Container or Application? Must depend on type probably. Container as default, for
    // example, for BeanBuildHook
    ///// Uhhh, Kan man definere den bare paa en container?? I don't think so. Must be Assembly or Bean // Or extension??
    // So either Bean or ContainerMirror
    Mirror declaredBy(); // Optional<BeanMirror>???

    /** {@return the assembly where the build hook is declared} */
    AssemblyMirror declaredByAssembly();

    /** {@return the class that implements the build hook} */
    Class<? extends BuildHook> hookClass();

    public enum DeclarationSite {
        ASSEMBLY,
        BEAN,
        DELEGATING_ASSEMBLY // BuildHook.Apply Maaske skal den i virkeligheden her hen
    }
}

interface ZandboxM {

    // MapStream would be nice
    Map<ComponentMirror, List<BuildHookMirror>> applicationSites();

    /**
     * {@return a set of assemblies where this transformer is being applied on at least one component defined by the
     * assembly.}
     */
    Set<AssemblyMirror> assemblies();

    /**
     * {@return a stream of components where this transformer is being applied, the type of components in the stream always
     * matched the transformer kind}
     */
    Stream<ComponentMirror> components();

    // I mean it would be nice to have some indication about how it was applied...
    Optional<StackTraceElement[]> declarationSite(); // Well it could be an annotation...

    /** {@return a tree of assemblies where this transformer is being evaluated for use.} */
    TreeView<AssemblyMirror> evaluatedAssemblies(); // needs better name, or maybe it is okay

    Optional<Class<? extends AssemblyPropagator>> propagator();

    List<BuildHookMirror> sieblings(ComponentPath path);
}

//I believe we have two modes.
//Either an annotation on A BuildSource or Bean
//Or via some of the open transform thingies

//Hmm, tror vi bliver noedt til at have 2 klasser her.
//En for BuildTransformeren og en for hvor den er Applied...
//Eller ogsaa skal vi ihvertfald ikke have next og previous... Det er jo forskelligt
//Om vi er paa assemblyen eller paa beanen.

//Skal vi have et <T extends TransformableComponent>??? Vel kun hvis skal have fat i T...

// ContainerMirror
//// Skal returnere hvilke mirrors der applier til den
//// Hvilke mirrors man sender videre til "undertyper"???? Fx default beans faettere. Men include det dem der er defineret paa Assembly???

// Tror ikke vi skal have nogen der mikser de do??? Nope. Saa maa man kombinere

//
//Optional<BuildTransformerMirror> previous();
//
//Optional<BuildTransformerMirror> next();

// Selve Transformeren... En per instans? Synten maaske det er en god regel...