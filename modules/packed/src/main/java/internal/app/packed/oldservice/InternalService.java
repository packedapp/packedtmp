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
package internal.app.packed.oldservice;

import java.util.function.Function;

import app.packed.service.Key;
import internal.app.packed.oldservice.build.ServiceSetup;
import internal.app.packed.oldservice.runtime.RuntimeService;
import internal.app.packed.oldservice.sandbox.Service;

/**
 *
 */
public sealed interface InternalService extends Service permits ServiceSetup, RuntimeService {

    public abstract InternalService rekeyAs(Key<?> key);

    public abstract <T> InternalService decorate(Function<? super T, ? extends T> decoratingFunction);
}
