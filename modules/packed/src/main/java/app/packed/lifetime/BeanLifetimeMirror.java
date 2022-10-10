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
package app.packed.lifetime;

import app.packed.bean.BeanMirror;
import internal.app.packed.lifetime.BeanLifetimeSetup;

/**
 * A bean lifetime represents a bean whose lifetime is independent of its container's lifetime.
 * <p>
 * A bean lifetime always has a container lifetime as a parent
 * 
 * <p>
 * Static and functional beans never have their own lifetime but will always their containers lifetime.
 * As they are valid as long as the container exists.
 */
public final class BeanLifetimeMirror extends LifetimeMirror {

    /** {@return the bean.} */
    public BeanMirror bean() {
        return beanLifetime().bean.mirror();
    }

    private BeanLifetimeSetup beanLifetime() {
        return (BeanLifetimeSetup) lifetime();
    }
}
