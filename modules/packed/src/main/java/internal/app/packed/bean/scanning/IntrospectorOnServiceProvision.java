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
package internal.app.packed.bean.scanning;

import java.util.Set;

import app.packed.bean.scanning.BeanIntrospector.OnContextService;
import app.packed.bean.scanning.BeanIntrospector.OnVariableUnwrapped;
import app.packed.binding.Key;
import app.packed.context.Context;

/**
 *
 */
public record IntrospectorOnServiceProvision(Key<?> key, Class<?> baseClass, Set<Class<? extends Context<?>>> contexts, OnVariableUnwrapped binder)
        implements OnContextService {

}
