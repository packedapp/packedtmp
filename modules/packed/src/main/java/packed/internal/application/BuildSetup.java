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
package packed.internal.application;

import app.packed.application.ApplicationDescriptor.ApplicationBuildType;
import app.packed.container.Wirelet;
import packed.internal.component.NamespaceSetup;
import packed.internal.component.RealmSetup;

/** A single build. */
public final class BuildSetup {

    /** The (root) application we are building. */
    public final ApplicationSetup application;

    /** The namespace this build belongs to. */
    public final NamespaceSetup namespace = new NamespaceSetup();

    /**
     * Creates a new build.
     * 
     * @param driver
     *            the application driver of the root (and often only) application
     * @param realm
     *            the realm of the application, has been created form the assembly or composer that describes the
     *            application
     * @param kind
     *            the build target
     * @param wirelets
     *            wirelets specified by the user. Prefixed with any wirelets from the application driver
     */
    public BuildSetup(PackedApplicationDriver<?> driver, ApplicationBuildType kind, RealmSetup realm, Wirelet[] wirelets) {
        this.application = new ApplicationSetup(this, kind, realm, driver, wirelets);
    }

}
//
//public boolean isDone() {
//  throw new UnsupportedOperationException();
//}
//
//public boolean isFailed() {
//  throw new UnsupportedOperationException();
//}
//
//public boolean isSuccess() {
//  throw new UnsupportedOperationException();
//}