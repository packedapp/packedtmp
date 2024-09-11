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
package app.packed.build.sandbox;

/**
 *
 */
// By profile we mean configuration, not profile the application.
// Basically we want to say if generate code eagerly or lazy

//// Possible ways to implement this
// Only on the root assembly
// Local to the assembly
// Inherited by child assemblies.
// Inherited by child assemblies. Which may override  settings

// This interface should not change the semantic meaning of applications
// Should it be overridable by Wirelets???

// CodegenProfile. Generate eagerly, lazily?? Why not just always lazily?
// Because of memory???
@interface CodegenProfile {

}
