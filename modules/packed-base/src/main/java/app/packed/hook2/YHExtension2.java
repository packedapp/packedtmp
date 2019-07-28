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
package app.packed.hook2;

import static java.util.Objects.requireNonNull;

import app.packed.component.ComponentConfiguration;
import app.packed.container.Extension;
import app.packed.hook.AnnotatedMethodHook;

/**
 *
 */
public class YHExtension2 extends Extension {

    @OnHook2(CacheBuilder.class)
    void doo(ComponentConfiguration cc, CachedObject co) {
        System.out.println(co);
    }
}

class CacheBuilder implements HookCacheBuilder<CachedObject> {

    private String val;

    /** {@inheritDoc} */
    @Override
    public CachedObject build() {
        return new CachedObject(val);
    }

    @OnHook2
    public void doo(AnnotatedMethodHook<YH> hook) {
        val = hook.annotation().value();
    }
}

class CachedObject {

    final String value;

    /**
     * @param value
     */
    public CachedObject(String value) {
        this.value = requireNonNull(value);
    }
}