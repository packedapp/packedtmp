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
package internal.app.packed.framework.devtools;

import java.lang.reflect.Member;

/**
 * This class integrates with the
 */
public abstract class PackedDevToolsIntegration {

    /** */
    public static final PackedDevToolsIntegration INSTANCE;

    static {
        // ServiceLoader adds ~ 12 ms to startup time
        
        // Maybe just a Class.forname()....
        
        INSTANCE = new NoDevTools();// ServiceLoader.load(PackedDevToolsIntegration.class).findFirst().orElseGet(() -> new NoDevTools());
    }

    public abstract void goo();

    public void reflectMembers(Class<?> clazz, Member[] members) {}

    /**
     * An instances of this class is provided from {@link PackedDevToolsIntegration#INSTANCE} when the devtools jar is not
     * on the classpath or modulepath.
     */
    static final class NoDevTools extends PackedDevToolsIntegration {

        public void goo() {
            System.out.println("No DevTools");
        }
    }
}
