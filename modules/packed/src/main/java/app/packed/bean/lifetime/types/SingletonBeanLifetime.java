/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package app.packed.bean.lifetime.types;

import app.packed.bean.lifetime.BeanLifetime;

/**
 * A lifetime that indicates that bean is created together with the application.
 * <p>
 * Static beans always have this lifetime
 */
// A single bean is created

// Was Application scope. Men passer jo ikke super godt med plugins.
// Til gaengaeld fungere Static bean nu ikke super godt
public class SingletonBeanLifetime extends BeanLifetime {

}
// Altsaa vi kan ikke have et plugin scope. Fordi så skal session jo også have et plugin scope


/**
 * A bean with a static scope
 */
// Maybe it is just application scope???
// I cannot see what the difference it
class StaticBeanScope extends BeanLifetime {

}
