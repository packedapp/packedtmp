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
package packed.internal.sidecar;

/**
 *
 */
public class ClassModel {

    Object[] data; // <--- ClassModels (Consts such as MethodDescriptors)

    MethodSidecarModel[] models; // just methods for now....

    MethodSidecarModel[] instantiations; // Models that needs to be instantiated (and written into ClassHolder instances

    MethodSidecarModel[] assemble; // sidecar that need to be invoked assemble

    // Their indexes into ClassHolder.instances() that must be invoked
    int[] assembleIndex;

    public ClassInstance newInstance() {
        return new ClassInstance(this, 0);
    }

    public static class Builder {
        int nextInstancePointer; // The pointer into ClassInstance.instances...
        // if needs method sidecar. create MethodSidecarModel
        // if non interface nextInstancePointer++
        // If method sidecar-> create models
    }
}
// Function0
// post(new Function1<FooBar, D>((a,v) -> fff)....
