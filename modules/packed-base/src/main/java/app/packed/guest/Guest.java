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
package app.packed.guest;

import java.util.concurrent.CompletableFuture;

import app.packed.lifecycleold.StopOption;

/**
 *
 */
// host facing

// Maybe GuestController.... And guest can be a view/readable thingy
public interface Guest {

    Guest start();

    <T> CompletableFuture<T> startAsync(T result);

    Guest stop(StopOption... options);

    <T> CompletableFuture<T> stopAsync(T result, StopOption... options);
}
