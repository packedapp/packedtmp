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
package app.packed.concurrent.job;

/**
 *
 */
// Som main men kan returnere et resultat.
// Aktivere JobExtensionen
// Dette betyder ogsaa at main skal vaere void

// JobHandler, Execute, ExecuteJob, idk
//@OnAnnotatedMethod(introspector = BeanIntrospector.class, allowInvoke = true)
public @interface ComputableJob {

    // Maybe it is just always OperationName... or Bean+OperationName
    // Only issue
    String jobName() default "main*";

    /// Entry Points

    // Main

    // CliCommand

    // @Get <- no result

    // @Compute(arg=foo);
}
