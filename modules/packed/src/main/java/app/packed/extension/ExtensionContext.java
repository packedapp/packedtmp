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
package app.packed.extension;

import app.packed.bean.BeanHook.TypedProvisionHook;
import app.packed.context.Context;
import internal.app.packed.lifetime.runtime.PackedExtensionContext;

/**
 * All beans that are owned by an extension operates within an ExtensionContext.
 * <p>
 * An instance of this class is typically required when invoking operations.
 */
@TypedProvisionHook(extension = BaseExtension.class)
public sealed interface ExtensionContext extends Context<BaseExtension> permits PackedExtensionContext {}
