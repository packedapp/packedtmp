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
package app.packed.bean.scanning;

/**
 *
 */
// Ideen var lidt vi kunne returnere noget ala
// List<Map.Entry<Class<?>, BeanTriggerType, BindingElement(Unmodifiable)>> BeanProxy.triggeredBy()

// Maybe skip Inheritable... It is only source
enum BeanTriggerType {
    ANNOTATED_CLASS, ANNOTATED_METHOD, ANNOTATED_FIELD, ANNOTATED_VARIABLE, CONTEXTUAL_SERVICE, INHERITABLE_CONTEXTUAL_SERVICE
}
