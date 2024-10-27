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
package app.packed.assembly;

import app.packed.bean.scanning.ForeignBeanTrigger.JavaBaseSupport;

/**
 *
 * <p>
 * This class is annotated with {@link app.packed.extension.BeanCustomHook.JavaBaseSupport} which provide various
 * default bindings for beans
 *
 * @apiNote We never return BaseAssembly from any method, as this would make the class difficult to extend.
 */
@JavaBaseSupport
//Open for testing?? Testing classes boer vaere i samme modul???
public abstract class BaseAssembly extends AbstractBaseAssembly {}
