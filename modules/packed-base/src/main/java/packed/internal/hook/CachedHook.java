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
package packed.internal.hook;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.hook.Hook;
import app.packed.lang.Nullable;

/**
 *
 */
// Bruges til at kalde tilbage paa extensions
final class CachedHook {

    private final Hook hook;

    private final MethodHandle mh;

    @Nullable
    private CachedHook next;

    /**
     * @param mh
     * @param hook
     */
    CachedHook(MethodHandle mh, Hook hook, @Nullable CachedHook next) {
        this.mh = requireNonNull(mh);
        this.hook = hook;
        this.next = next;
    }

    public Hook hook() {
        return hook;
    }

    public MethodHandle mh() {
        return mh;
    }

    @Nullable
    public CachedHook next() {
        return next;
    }
}
