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
package app.packed.component;

import app.packed.attribute.Attribute;

/**
 *
 */
public interface SystemX {

    /**
     * Counts and returns the number of components in the system. This is usually not a constant-time operation.
     * 
     * @return the number of components in the system
     */
    default long count() {
        return stream().count();
    }

    /**
     * Returns the root component of the system.
     * 
     * @return the root component of the system
     */
    Component root();

    ComponentStream stream(ComponentStream.Option... options);

    public static void main(Component c) {
        String s = c.attributes().get(Attribute.DESCRIPTION);

        c.attributes().ifPresent(Attribute.DESCRIPTION, e -> System.out.println(e));

        System.out.println(s);
    }

    // Returns et configurations system... ikke et running system.
    static SystemX of(Bundle<?> b) {
        throw new UnsupportedOperationException();
    }

    // Option? Ways to construct a system. snapshot()
    // AS.system(Option.snapshot());
}
