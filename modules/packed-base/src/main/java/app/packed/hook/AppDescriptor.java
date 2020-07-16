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
package app.packed.hook;

/**
 *
 */
// Like BundleDescriptor...
// Altsaa har vi maaske en generics 
// ArtifactView.... ContainerView??? Altsaa jeg App er vel fin...

// app.descriptor();

// AppDescriptor AppDescriptor.of(image);
// AppDescriptor AppDescriptor.of(bundle);

// ArtifactDescriptor???? Jeg ved ikke hvad den har specielt....

interface AppDescriptor {

}

// image.initializationoperations.print();

// LifecycleOperation...> invoke method, constructor, read/write field, function

// Instantiate FooImpl#()
// Invoke FooImpl#ddddd via @OnInitialze
// 

/// Parallism.... ved start/stop...

// Assemble [serial] initialize [serial] start/stop [parallel]

//TreeView<ArtifactDescriptor> ofSystem(...);