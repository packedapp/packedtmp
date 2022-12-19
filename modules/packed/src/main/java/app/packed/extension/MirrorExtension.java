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
import app.packed.container.AssemblyMirror;
import app.packed.container.ContainerMirror;
import app.packed.operation.OperationMirror;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.IntrospectedBeanBinding;
import internal.app.packed.operation.OperationSetup;

/**
 * An extension that can be used to provide mirror instances at runtime.
 * <p>
 * This extension is used to inject mirrors of type {@link ApplicationMirror}, {@link ContainerMirror} or
 * {@link BeanMirror} at runtime.
 * <p>
 * This extension is mainly here as a kind of "marker extension". Indicating that somewhere in the application someone
 * has decided to reference a mirror. In which case the mirroring of the whole application is available at runtime.
 * <p>
 * Maybe at some point we will support a compact mirror mode where each extension can keep a minimal set of information
 * that is needed at runtime.
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
     * Creates bindings for {@link ApplicationMirror}, {@link AssemblyMirror}, {@link ContainerMirror}, {@link BeanMirror},
     * and {@link OperationMirror}.
     * 
     * {@inheritDoc}
     */
    @Override
    protected BeanIntrospector newBeanIntrospector() {
        return new BeanIntrospector() {

            @Override
            public void onBinding(OnBinding binding) {
                IntrospectedBeanBinding ibb = ((IntrospectedBeanBinding) binding);
                OperationSetup operation = ibb.operation;
                BeanSetup bean = ibb.operation.bean;
                if (binding.hookClass() == ApplicationMirror.class) {
                    binding.bind(bean.container.application.mirror());
                } else if (binding.hookClass() == AssemblyMirror.class) {
                    binding.bind(bean.container.assembly.mirror());
                } else if (binding.hookClass() == ContainerMirror.class) {
                    binding.bind(bean.container.mirror());
                } else if (binding.hookClass() == BeanMirror.class) {
                    binding.bind(bean.mirror());
                } else if (binding.hookClass() == OperationMirror.class) {
                    binding.bind(operation.mirror());
                } else {
                    super.onBinding(binding);
                }
            }
        };
    }
}

//https://docs.scala-lang.org/overviews/reflection/environment-universes-mirrors.html
//reflect = build time, introspect = runtime.. IDK
enum MirrorEnvironment { // ApplicationEnvironment???
    BUILD_TIME, RUN_TIME;
}
