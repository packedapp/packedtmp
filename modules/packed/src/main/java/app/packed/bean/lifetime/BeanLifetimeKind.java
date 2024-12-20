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
package app.packed.bean.lifetime;

/**
 *
 */
// Region -> Group
public enum BeanLifetimeKind {

    /** 1 or more beans. That are all initialized together (unless lazy). */
    REGION,

    /** The bean is created independently of any other beans.*/
    BEAN,

    /** The runtime creates a new bean instance for the duration of a single operation. */
    OPERATION;
}
