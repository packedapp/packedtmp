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
package packed.internal.inject.service;

import java.util.Map;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.bundle.ExtensionMember;
import app.packed.component.Wirelet;
import app.packed.inject.ServiceExtension;
import packed.internal.inject.service.build.BuildtimeService;

/**
 *
 */
@ExtensionMember(ServiceExtension.class)
public abstract class Service2ndPassWirelet extends Wirelet {
    protected abstract void process(@Nullable ServiceComposer parent, ServiceComposer child, Map<Key<?>, BuildtimeService> map);
}