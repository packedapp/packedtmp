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
package app.packed.container.sandbox;

/**
 *
 */
public enum ContainerKind {

    /** Will start a new managed container lifetime. */
    NEW_MANAGED,

    /** Will start a new unmanaged container lifetime. */
    NEW_UNMANAGED,

    /** Will use the lifetime from the parent container.*/
    FROM_PARENT;
}
