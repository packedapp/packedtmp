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
package internal.app.packed.lifecycle.runtime;

import java.util.concurrent.TimeUnit;

import app.packed.application.ApplicationContext;
import app.packed.lifecycle.RunState;
import app.packed.lifecycle.sandbox.StopOption;

/**
 *
 */
public final class PackedApplicationContext implements ApplicationContext {

    /** {@inheritDoc} */
    @Override
    public String name() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void sleep(RunState state) throws InterruptedException {}

    /** {@inheritDoc} */
    @Override
    public boolean sleep(RunState state, long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public RunState currentState() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public RunState desiredState() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isManaged() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean stopAsync(StopOption... options) {
        return false;
    }

}
