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
package app.packed.component;

import java.util.Set;

import app.packed.application.ApplicationMirror;
import app.packed.assembly.AssemblyMirror;
import app.packed.bean.BeanMirror;
import app.packed.build.BuildAuthority;
import app.packed.build.BuildMirror;
import app.packed.container.ContainerMirror;
import app.packed.namespace.NamespaceMirror;
import app.packed.operation.OperationMirror;
import app.packed.util.TreeView;

/**
 * A mirror representing a component, which is the basic building block in the framework.
 * <p>
 * A component mirror is always defined either by the framework or by an extension. IDK would it make sense to have it
 * as a user??? Fx Importer...
 */
public sealed interface ComponentMirror extends BuildMirror permits ApplicationMirror, BeanMirror, ContainerMirror, NamespaceMirror, OperationMirror {

    // BuildAction installedBy();?? Cute

    // All components have names? IDK. Maybe they should!!
    // Maybe you cannot set it though fx app.name=rootcontainer.name
    // String name();

    /** {@return the kind of the component} */
    default ComponentKind componentKind() {
        return componentPath().componentKind();
    }

    /** {@return the path of the component} */
    // Giver mening hvis vi faar Applications.components();
    default BuildAuthority componentOwner() {
        throw new UnsupportedOperationException();
    }

    /** {@return the path of the component} */
    ComponentPath componentPath();

    /** {@return a set of any tags that have been set on the component} */
    default Set<String> componentTags() {
        return Set.of();
    }

    /**
     * Represents one or more containers ordered in a tree with a single node as the root.
     * <p>
     * Unless otherwise specified the tree is ordered accordingly to the installation order of each container.
     */
    // TODO make sealed, currently there is a bug in Eclipse
    public interface OfTree extends TreeView<ComponentMirror> {

        // Must form a tree as well
        default AssemblyMirror.OfTree assemblies() {
            throw new UnsupportedOperationException();
        }
        // Maaske er det en enum?
        // isAllOfApplication
        // isPartialSubtreeOfApplication();

    }
    /** {@return the build step that installed the component} */
    // Installation is a runtime concept... You don't install an extension, you add it
    // What about assemblies, you link them??
//    default BuildStepMirror installationStep() {
//        throw new UnsupportedOperationException();
//    }
}
