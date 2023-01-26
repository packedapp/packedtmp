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
package app.packed.concurrent.scheduling;

/**
 *
 */
class PackedSchedulingContext implements SchedulingContext {

    final PackedRunner runner;

    PackedSchedulingContext(PackedRunner runner) {
        this.runner = runner;
    }

    /** {@inheritDoc} */
    @Override
    public void pause() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void resume() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void cancel() {
        runner.cancelSoft();
    }

    /** {@inheritDoc} */
    @Override
    public long invocationCount() {
        return runner.count.get();
    }
}
