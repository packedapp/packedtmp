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
package packed.internal.bean.hooks.usesite;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Modifier;

import app.packed.base.Nullable;
import app.packed.component.BuildException;
import packed.internal.bean.BeanSetup;
import packed.internal.bean.inject.DependencyNode;
import packed.internal.bean.inject.DependencyProducer;
import packed.internal.inject.service.ContainerInjectionManager;
import packed.internal.inject.service.build.BeanMemberServiceSetup;
import packed.internal.inject.service.build.ServiceSetup;
import packed.internal.lifetime.LifetimePoolSetup;
import packed.internal.lifetime.PoolEntryHandle;

/**
 *
 */
public final class BeanMemberDependencyNode extends DependencyNode {

    @Nullable
    protected final UseSiteMemberHookModel sourceMember;

    @Nullable
    protected final BeanMemberServiceSetup service;
    
    public BeanMemberDependencyNode(BeanSetup source, UseSiteMemberHookModel smm, DependencyProducer[] dependencyProviders) {
        super(source, smm.dependencies, smm.methodHandle(), dependencyProviders);

        if (smm.provideAskey != null) {
            if (!Modifier.isStatic(smm.getModifiers()) && bean.singletonHandle == null) {
                throw new BuildException("Not okay)");
            }
            ContainerInjectionManager sbm = bean.parent.beans.getServiceManagerOrCreate();
            ServiceSetup sa = this.service = new BeanMemberServiceSetup(sbm, bean, this, smm.provideAskey, smm.provideAsConstant);
            sbm.addService(sa);
        } else {
            this.service = null;
        }
        
        this.sourceMember = requireNonNull(smm);

        if (!Modifier.isStatic(smm.getModifiers())) {
            dependencyProviders[0] = bean;
        }
    }

    @Nullable
    protected PoolEntryHandle poolAccessor() {
        // buildEntry is null if it this Injectable is created from a source and not @AtProvides
        // In which case we store the build entry (if available) in the source instead
        if (service != null) {
            return service.accessor;
        }
        return null;
    }

    // All dependencies have been successfully resolved
    /**
     * All of this consumers dependencies have been resolved
     * 
     * @param pool
     */
    public void onAllDependenciesResolved(LifetimePoolSetup pool) {
        super.onAllDependenciesResolved(pool);

        if (bean.singletonHandle != null) {
            // Maybe shared with SourceAssembly

            if (sourceMember.provideAskey == null) {
                MethodHandle mh1 = runtimeMethodHandle();

                // RuntimeRegionInvoker
                // the method on the sidecar: sourceMember.model.onInitialize

                // MethodHandle(Invoker)void -> MethodHandle(MethodHandle,RuntimeRegion)void
                if (sourceMember instanceof UseSiteMethodHookModel msm) {
                    System.out.println(mh1);
//                    if (msm.bootstrapModel.onInitialize != null) {
//                        // System.out.println(msm.model.onInitialize);
//                        MethodHandle mh2 = MethodHandles.collectArguments(msm.bootstrapModel.onInitialize, 0, LifetimePoolMethodAccessor.MH_INVOKER);
//
//                        mh2 = mh2.bindTo(mh1);
//
//                        bean.application.container.lifetime.initializers.add(mh2);
//                    }
                }
            }
        }
    }
}
