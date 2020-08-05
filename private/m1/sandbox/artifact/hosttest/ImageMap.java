/*
 * Copyright (c) 2008 Kasper Nielsen.
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
package sandbox.artifact.hosttest;

import app.packed.artifact.Image;
import app.packed.base.Key;
import app.packed.component.Wirelet;

/**
 *
 */
//MultiContainer perhaps???
//Only needed if we have more than one image.
//Or we want to upgrade/replace the image
// All images are stored as image children of the host...
// With a key...

//Maybe this is just part of Host Context instead of its own interface
// imageKeys();
// imageStart();
// DefaultKey? Type + image name???

//HostContext
// Set<Key<?>> imageKeys();
// Image<T> useImage(Class<T>)
// Image<T> useImage(Key<T>)

public interface ImageMap {

    // if execution context will start
    // otherwise just create
    // im.start(App.class);
    <T> Image<T> use(Class<T> type);

    /**
     * Returns the set of keys for which an image is registered.
     * 
     * @return the set of keys for which an image is registered
     */
    Key<?> keys();

    default <T> T start(Class<T> key, Wirelet... wirelets) {
        return start(Key.of(key), wirelets);
    }

    <T> T start(Key<T> type, Wirelet... wirelets);

    // CompFuture
    <T> T startAsync(Class<T> type, Wirelet... wirelets);
}
