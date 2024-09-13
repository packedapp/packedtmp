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
package internal.app.packed.application;

import java.util.Set;
import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import app.packed.assembly.AssemblyMirror;
import app.packed.bean.BeanMirror;
import app.packed.build.BuildMirror;
import app.packed.container.ContainerMirror;
import app.packed.extension.BaseExtension;
import app.packed.extension.BeanTrigger.BindingClassBeanTrigger;
import app.packed.extension.Extension;
import app.packed.lifetime.ContainerLifetimeMirror;

/**
 *
 */
// A deployment is basically a tree of applications that have been built together

// We may have multiple deployments

// Family < Deployment < Application < Container < Bean < Operation < Binding | Interceptor

//Cluster|Node? < Java Process(Logical name) < Family
@BindingClassBeanTrigger(extension = BaseExtension.class)
public class DeploymentMirror implements BuildMirror {

    /** The deployment we are mirroring. */
    private final DeploymentSetup deployment;

    /**
     * Create a new deployment mirror.
     *
     * @throws IllegalStateException
     *             if attempting to explicitly construct a deployment mirror instance
     */
    public DeploymentMirror(DeploymentSetup setup) {
        // Will fail if the deployment mirror is not initialized by the framework
        this.deployment = setup;
    }

    /** {@return a tree of all the applications that make of the deployment.} */
    public ApplicationMirror.OfTree applications() {
        throw new UnsupportedOperationException();
    }

    /** {@return a tree of all the assemblies that make of the deployment.} */
    public AssemblyMirror.OfTree assemblies() {
        throw new UnsupportedOperationException();
    }

    public Stream<BeanMirror> beans() {
        throw new UnsupportedOperationException();
    }

    /** {@return a tree of all the containers that make of the deployment.} */
    public ContainerMirror.OfTree containers() {
        throw new UnsupportedOperationException();
    }

    /** {@return an unmodifiable {@link Set} view of every extension type that has been used in the deployment.} */
    // Er super svaert at lave et view her.
    // Vi skal kombinere multiple map key sets
    public Set<Class<? extends Extension<?>>> extensionTypes() {
        throw new UnsupportedOperationException();
    }

    // Er som udgangspunkt "syntetisk" og alt information er ikke med
    public ApplicationMirror hostApplication() {
        throw new UnsupportedOperationException();
    }

    public ContainerLifetimeMirror.OfTree lifetimes() {
        throw new UnsupportedOperationException();
    }

    public String name() {
        return deployment.root.mirror().name();
    }

    public Stream<BeanMirror> operations() {
        throw new UnsupportedOperationException();
    }

}

////Den har ikke et navn. Fordi vi er jo ikke unik per Java Process
//Namespace
//Tenant
//Isolate
//Zone
//Domain
//Space
//Guest
//Host
//Frame?
//Suite

//Tenant’s configuration objects are grouped under namespaces. Namespaces can be thought of as administrative domains. All the objects of the same kind need to have unique names in a given namespace. Namespace themselves must be unique within a tenant. In this document namespace will be referred as <tenant>/<namespace>, which will be globally unique.
//
//Every object’s unique identity is {kind,tenant,namespace,name}. This is called fully qualified name(FQN) of the object. When a user or other objects refers to an object, FQN is required. Many times the tenant field can be omitted as it is derived from login credentials. So tenants usually refer to objects as {kind, namespace, object}.
