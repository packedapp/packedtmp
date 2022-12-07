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
package app.packed.bean;

import app.packed.context.Context;

/**
 * A context for selected operations if a bean has been configured with initialization elements.
 * <p>
 * If a bean is not initialized with any values, the context is not present.
 * 
 * @see InstanceBeanConfiguration#initializeWithInstance(Class, Object)
 * @see InstanceBeanConfiguration#initializeWithInstance(app.packed.service.Key, Object)
 */
public interface InitializeWithContext extends Context<BeanExtension> {}
