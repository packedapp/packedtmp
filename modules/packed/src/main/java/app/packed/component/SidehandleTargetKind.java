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
package app.packed.component;

/**
 *
 */
// For now we have all 6.
// Application and Container and Namespace are probably not super important.
// But maybe a special SidebeanLifecycle can be useful for better control of @Stop, @Start
public enum SidehandleTargetKind {
    APPLICATION,
    CONTAINER,

    // Altsaa den er jo en del anderledes. Eftersom jeg taenker vi kun laver den en gang.
    // Eller hmm, Naar vi laver et request er del invoker.invoke(Req, Res)
    LIFETIME,
    NAMESPACE,
    BEAN,

    /** The target for a sidebean is an operation. */
    OPERATION;
}
