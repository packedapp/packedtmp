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
package app.packed.job;

import java.time.Duration;

/**
 *
 */
// ScheduledJob/JobProducer/etc..
// JobRequest, JaxRS Request, ect.
// Job er et slags request

// /ScheduledJobName/212 <- instance number

// Extends Entity????
public interface Request {

    Duration getDuration();

    // Hiercharcical jobs

    // DifferentId, for example, concat

    // Entity getParent();???? Ideen er f.eks. at en component som requested udspring fra kan vaere parenten.
    Request getParent();

    Request spawnChild();

    // Local or for parent/children as well
    // Maybe write this level, read this, then parent, then grandparent ect

}
