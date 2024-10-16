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
package app.packed.bean.lifecycle;

/**
 *
 * @see OnInitialize
 * @see OnStart
 * @see OnStop
 */
// BeanLifecycleOrder
// DependencyOrder <---
// In app.packed.lifetime/lifecycle?

//PreOrder, PostOrder | OperationDependencyORder->DependencyOrder (Or just Ordering)
public enum LifecycleDependantOrder {

    /** The operation will be executed before any other operation on beans that have this bean as a dependency. */
    BEFORE_DEPENDANTS,

    /** The operation will be executed after any dependencies. */
    AFTER_DEPENDANTS;
}
