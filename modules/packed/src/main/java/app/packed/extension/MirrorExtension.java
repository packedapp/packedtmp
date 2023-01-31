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
package app.packed.extension;

import app.packed.application.ApplicationMirror;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanMirror;
import app.packed.bindings.BindableWrappedVariable;
import app.packed.container.AssemblyMirror;
import app.packed.container.ContainerMirror;
import app.packed.operation.OperationMirror;
import internal.app.packed.bean.PackedBindableBaseVariable;
import internal.app.packed.operation.OperationSetup;

/**
 * An extension that can be used to provide mirror instances at runtime.
 * <p>
 * This mirrors that can be injected at runtime are {@link ApplicationMirror}, {@link ContainerMirror},
 * {@link AssemblyMirror}, {@link BeanMirror} and {@link OperationMirror}.
 * <p>
 * Something about we include everything.
 * This extension is mainly here as a kind of "marker extension". Indicating that somewhere in the application someone
 * has decided to reference a mirror. In which case mirrors for the whole application is available at runtime.
 * <p>
 * At some point in the future we might support a compact mirror mode where each extension only keep a minimal set of
 * information at runtime.
 *
 * @see ApplicationMirror
 * @see ContainerMirror
 * @see AssemblyMirror
 * @see BeanMirror
 * @see OperationMirror
 */
public class MirrorExtension extends FrameworkExtension<MirrorExtension> {

    /** Create a new mirror extension. */
    MirrorExtension() {}

    /**
     * Creates bindings for {@link ApplicationMirror}, {@link ContainerMirror}, {@link AssemblyMirror},{@link BeanMirror},
     * and {@link OperationMirror}.
     *
     * {@inheritDoc}
     */
    @Override
    protected BeanIntrospector newBeanIntrospector() {
        return new BeanIntrospector() {
            @Override
            public void hookOnProvidedVariableType(Class<?> hook, BindableWrappedVariable binding) {
                OperationSetup operation = ((PackedBindableBaseVariable) binding).v.operation;

                if (hook == ApplicationMirror.class) {
                    binding.bindConstant(operation.bean.container.application.mirror());
                } else if (hook == ContainerMirror.class) {
                    binding.bindConstant(operation.bean.container.mirror());
                } else if (hook == AssemblyMirror.class) {
                    binding.bindConstant(operation.bean.container.assembly.mirror());
                } else if (hook == BeanMirror.class) {
                    binding.bindConstant(operation.bean.mirror());
                } else if (hook == OperationMirror.class) {
                    binding.bindConstant(operation.mirror());
                } else {
                    // binding.unwrap(BindingTracer.class);

                    // Not a supported mirror type, let checkAssignableTo throw an exception
                    binding.checkAssignableTo(ApplicationMirror.class, ContainerMirror.class, AssemblyMirror.class, BeanMirror.class, OperationMirror.class);
                }
            }
        };
    }
}
