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
package packed.internal.application.entrypoint;

import app.packed.application.entrypoint.EntryPointExtension;
import app.packed.extension.ApplicationExtensor;
import packed.internal.application.ApplicationSetup;

/**
 *
 */

// ApplicationSetup: EntrypointApplicationExtensor entrypoints 
public final class EntrypointApplicationExtensor extends ApplicationExtensor<EntryPointExtension> {

    EntrypointApplicationExtensor(ApplicationSetup a) {
        // a.entryPoints = this;
    }

    @Override
    protected void onComplete() {
        super.onComplete();
    }

    @Override
    protected void onFirst(EntryPointExtension extension) {
        super.onFirst(extension);
    }
}
