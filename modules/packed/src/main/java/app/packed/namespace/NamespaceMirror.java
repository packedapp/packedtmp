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
package app.packed.namespace;

import static java.util.Objects.requireNonNull;

import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import app.packed.assembly.AssemblyMirror;
import app.packed.bean.BeanMirror;
import app.packed.bean.BeanTrigger.AutoService;
import app.packed.binding.Key;
import app.packed.component.ComponentRealm;
import app.packed.container.ContainerMirror;
import app.packed.operation.OperationMirror;
import app.packed.util.TreeView;
import internal.app.packed.bean.introspection.IntrospectorOnAutoService;
import internal.app.packed.extension.base.BaseExtensionBeanIntrospector;
import internal.app.packed.namespace.NamespaceSetup;
import internal.app.packed.util.PackedTreeView;
import internal.app.packed.util.accesshelper.AccessHelper;
import internal.app.packed.util.accesshelper.NamespaceMirrorAccessHandler;

/**
 * A mirror representing a namespace.
 */
@AutoService(introspector = NamespaceMirrorBeanIntrospector.class)
public final class NamespaceMirror {

    static {
        AccessHelper.initHandler(NamespaceMirrorAccessHandler.class, new NamespaceMirrorAccessHandler() {
            @Override
            public NamespaceMirror newNamespaceMirror(NamespaceSetup namespace) {
                return new NamespaceMirror(namespace);
            }
        });
    }

    private final NamespaceSetup namespace;

    NamespaceMirror(NamespaceSetup namespace) {
        this.namespace = requireNonNull(namespace);
    }

    public ApplicationMirror application() {
        return namespace.rootContainer.application.mirror();
    }

    /** {@return the assembly wherein this container was defined.} */
    public AssemblyMirror assembly() {
        return namespace.rootContainer.assembly.mirror();
    }


    /** {@return a stream of all of the bean declared by the user in the application.} */
    public Stream<BeanMirror> beans() {
        return containers().stream().flatMap(ContainerMirror::beans);
    }

    /** {@return a mirror of the root container in the application.} */
    public ContainerMirror container() {
        return namespace.rootContainer.mirror();
    }

    public ComponentRealm owner() {
        return namespace.owner;
    }

    /** {@return a container tree mirror representing all the containers defined within the application.} */
    // For extensions, all the containers in which it is used!!!
    public TreeView<ContainerMirror> containers() {
        return new PackedTreeView<>(namespace.rootContainer, c -> c.namespace == namespace, c -> c.mirror());
    }

    /** {@return a stream of all of the operations on beans owned by the user in the application.} */
    // I think non-synthetic should also be filtered
    public OperationMirror.OfStream<OperationMirror> operations() {
        return OperationMirror.OfStream.of(beans().flatMap(BeanMirror::operations));
    }
}

final class NamespaceMirrorBeanIntrospector extends BaseExtensionBeanIntrospector {

    @Override
    public void onExtensionService(Key<?> key, IntrospectorOnAutoService service) {
        service.binder().bindConstant(service.bean().container.namespace.mirror());
    }
}