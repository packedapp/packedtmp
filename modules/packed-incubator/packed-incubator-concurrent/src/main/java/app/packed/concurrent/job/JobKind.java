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
package app.packed.concurrent.job;

/**
 *
 */
// We har typisk de her 4 job typer
public enum JobKind {

    // A single job defining the lifetime of the lifetime.
    LIFETIME_JOB,  // ComputableJob

    // Was Daemon job
    BACKGROUND_JOB,

    // ScheduledJob that it only run once
    SCHEDULED_ONCE_JOB,

    // A recurrent job
    RECURRENT_JOB,

    // A submitted job, we probally can only define this at runtime
    ONE_SHOT_JOB
}
