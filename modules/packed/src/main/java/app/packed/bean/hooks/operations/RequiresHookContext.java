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
package app.packed.bean.hooks.operations;

import app.packed.bean.hooks.operations.RequiresHookContext.Kind;

/**
 *
 */
public @interface RequiresHookContext {

    Kind kind();

    // must be in the same packed as the extension, or maybe not
    // Anyone can choose to support HttpRequestContext... for their operations/beans
    Class<?> contextType();

    String errorMsg();

    enum Kind {
        CONTAINER, BEAN, OPERATION;
    }
}

@RequiresHookContext(kind = Kind.BEAN, contextType = EntityBeanContext.class, errorMsg = "@UsageEntityManagerMethodHook can only be used with entity beans")
// "@ExtractHeader can only be used within the context off ");
class UsageEntityManagerMethodHook {}

interface EntityBeanContext {}
