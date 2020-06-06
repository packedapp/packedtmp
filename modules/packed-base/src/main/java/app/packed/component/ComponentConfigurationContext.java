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
 * A context object used by {@link AbstractComponentConfiguration}.
 *
 * @apiNote In the future, if the Java language permits, {@link ComponentConfigurationContext} may become a
 *          {@code sealed} interface, which would prohibit subclassing except by explicitly permitted types.
 */
public interface ComponentConfigurationContext extends ComponentConfiguration {

}
