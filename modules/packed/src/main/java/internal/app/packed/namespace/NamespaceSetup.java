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
package internal.app.packed.namespace;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import app.packed.component.ComponentPath;
import app.packed.namespace.NamespaceHandle;
import app.packed.namespace.NamespaceMirror;
import internal.app.packed.build.AuthoritySetup;
import internal.app.packed.component.ComponentSetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/**
 *
 */
// Is an application a namespace for Components??? My brain just fried
@SuppressWarnings("rawtypes")
public final class NamespaceSetup implements ComponentSetup {

    /** The default name of a namespace. */
    public static final String DEFAULT_NAME = "main";

    /** A handle for invoking the protected method {@link NamespaceHandle#onNamespaceClose()}. */
    private static final MethodHandle MH_HANDLE_ON_NAMESPACE_CLOSE = LookupUtil.findVirtual(MethodHandles.lookup(), NamespaceHandle.class, "onNamespaceClose",
            void.class);

    /** A handle that can access {@link NamespaceHandle#namespace}. */
    private static final VarHandle VH_NAMESPACE_HANDLE_TO_SETUP = LookupUtil.findVarHandle(MethodHandles.lookup(), NamespaceHandle.class, "namespace",
            NamespaceSetup.class);

    NamespaceHandle handle;

    // Must search up until root to find local names
    final Map<ContainerSetup, String> localNames = new HashMap<>();

    /** The name of the namespace. */
    public String name = DEFAULT_NAME;

    /** All operations defined in this namespace. */
    public final ArrayList<OperationSetup> operations = new ArrayList<>();

    /** The owner of the name space. */
    public final AuthoritySetup owner;

    /** The extension and root container of the namespace. */
    public final ExtensionSetup root;

    /** The namespace template */
    public final PackedNamespaceTemplate template;

    public NamespaceSetup(PackedNamespaceTemplate template, ExtensionSetup root, AuthoritySetup owner) {
        this.template = template;
        this.root = root;
        this.owner = owner;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath componentPath() {
        throw new UnsupportedOperationException();
    }

    /** { @return the handle of the namespace} */
    public NamespaceHandle handle() {
        return requireNonNull(handle);
    }

    /** Call {@link Extension#onAssemblyClose()}. */
    public void invokeNamespaceOnNamespaceClose() {
        try {
            MH_HANDLE_ON_NAMESPACE_CLOSE.invokeExact(handle);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
    }

    /** {@inheritDoc} */
    @Override
    public NamespaceMirror<?> mirror() {
        return handle().mirror();
    }

    /** {@return the name of the namespace} */
    public String name() {
        return name;
    }

    public static NamespaceSetup crack(NamespaceHandle<?, ?> handle) {
        return (NamespaceSetup) VH_NAMESPACE_HANDLE_TO_SETUP.get(handle);
    }

    public record NamespaceKey(Class<? extends NamespaceHandle<?, ?>> handleClass, String name) {}
}
