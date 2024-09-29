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
 * A bean with a transient bean scope is a bean that is create within the context of a single operation. But once
 * created the framework will no longer keeps track of the bean. As a consequence beans with transient scope does not
 * support destructive lifecycle operations such as {@link app.packed.lifetime.OnStop}.
 * <p>
 * A typical example is transient services,
 *
 * Maaske hvis en der viser klokken.
 *
 * A transient service can be both a bean and the result of an operation
 *
 */
public class TransientBeanScope extends BeanLifetime {

}
