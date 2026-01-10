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
package internal.app.packed.application.repository;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import app.packed.application.ApplicationHandle;

public final class InstallingApplicationLauncher<I, H extends ApplicationHandle<I, ?>> implements ApplicationLauncherOrFuture<I, H> {

    final CountDownLatch cdl = new CountDownLatch(1);

    String name = UUID.randomUUID().toString();

    public InstallingApplicationLauncher() {

    }

}