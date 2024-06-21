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
package app.packed.container;

/**
 *
 */
public class WireletSettings {
    static final WireletSettings DEFAULTS = null;

    public WireletSettings siteApplication() {
        return this;
    }

    public WireletSettings siteContainer() {
        return this;
    }

    // Where/When can it be specified

    public enum ApplicationSite {

        /** Can only be specified for the root container of the application. */
        APPLICATION_ROOT,

        /** Can be specified at any link site. */
        ANY_CONTAINER;

        // NamespaceRoot/DomainRoot   (Class+Name?)
    }

    // Maybe this is more, when should the Wirelet be available
    // Build_time -> well you can only specify it at build time
    public enum SpecificationTime {

        /** Can only be specified for the root container of the application. */
        BUILD_TIME,

        /** Can be specified at any link site. */
        LAUNCH_TIME;
    }

    /////////// SCOPE

    // Can only specify it for lifetime roots at runtime... For example, Session

    // Is related to site I would guess
    // Should also be related to ServiceNamespace?
    //// Probably not, if container -> should only be container


    // Where can it be seen (Maybe like Site... The Service Domain??)
    // How is it stored, processed
    // What if unprocessed?

    // Maybe wirelets are never available after launch time???
    //// Which would mean it should always be on the application lifetime level.
    //// Or the same lifetime level as it was spawned.
    //// But then we wouldn't fail on a session wirelet (that the user assumed was specified at application launch time)
    //// Because we were open to it being specified when launching the session

    ///// OKAY OKAY
    // Can only consume a given wirelet a single place in scope???
    // But then hmm, What about super classes / WireletSelection?
    // We can actually catch those. There is a wirelet selection<AbstractWirelet> that catches the same wirelet
    // But what about at runtime?? Der giver den jo faktisk mening, som et flag der kan laeses flere gange
}
