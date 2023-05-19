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
package app.packed.application;

import java.util.Set;

import app.packed.container.AssemblyTreeMirror;
import app.packed.container.ContainerTreeMirror;
import app.packed.extension.Extension;
import app.packed.lifetime.ContainerLifetimeTreeMirror;
import internal.app.packed.container.Mirror;

/**
 *
 */
// A deployment is basically a tree of applications.

// We may have multiple deployments

//Cluster|Node? < Java Process < Deployment < Application < Container < Bean < Operation < Binding | Interceptor

public interface DeploymentMirror extends Mirror {

    /** {@return a tree of all the applications that make of the deployment.} */
    ApplicationTreeMirror applications();

    /** {@return a tree of all the assemblies that make of the deployment.} */
    AssemblyTreeMirror assemblies();

    /** {@return a tree of all the containers that make of the deployment.} */
    ContainerTreeMirror containers();

    /** {@return an unmodifiable {@link Set} view of every extension type that has been used in the deployment.} */
    Set<Class<? extends Extension<?>>> extensionTypes();

    ContainerLifetimeTreeMirror lifetimes();

    // Er som udgangspunkt "syntetisk" og alt information er ikke med
    ApplicationMirror hostApplication();
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
