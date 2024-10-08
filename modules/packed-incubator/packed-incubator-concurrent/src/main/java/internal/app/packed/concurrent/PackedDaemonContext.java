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
package internal.app.packed.concurrent;

import java.util.concurrent.TimeUnit;

import app.packed.concurrent.DaemonContext;

/**
 *
 */
public class PackedDaemonContext implements DaemonContext {

    /** {@inheritDoc} */
    @Override
    public boolean isShutdown() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void awaitShutdown(long timeout, TimeUnit unit) throws InterruptedException {
        System.out.println("AWAIT");
    }

    /** {@inheritDoc} */
    @Override
    public void await() throws InterruptedException {
        System.out.println("AWAIT");
    }

}
