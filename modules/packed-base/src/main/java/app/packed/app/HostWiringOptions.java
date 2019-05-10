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
package app.packed.app;

/**
 *
 */
// I think

// Maybe extend WiringOption.. so we can restrict which options you can use....
// But would be nice to be able to specify default options for a host..
// Maybe even a Function<Secrets, List<HostWiringOption>
public class HostWiringOptions {
    // UNDEPLOY_ON_TERMINATION or
    // KEEP_ON_TERMINATION

    // TIME_TO_LIVE

    // AFTER_UNDEPLOY_RETENTION_TIME (LINGER??) basically keep this around for a minute

    // MHT til naming, kan vi f.eks. bruge et prefix: Session-UUID
}
