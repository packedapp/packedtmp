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
package app.packed.framework;

import java.util.Set;

/** This class contains information about the framework. */
public final class Framework {

    /** No framework for you. */
    private Framework() {}

    /** {@return a set of module names that make up the framework.} */
    public static Set<String> moduleNames() {
        return FrameworkProps.MODULE_NAMES;
    }

    /** {@return the name of the framework.} */
    public static String name() {
        return FrameworkProps.NAME;
    }

    /** {return the version of the frame as a {@link Version}.} */
    public static Framework.Version version() {
        throw new UnsupportedOperationException();
    }

    public static final class Version implements Comparable<Version> {

        /** {@inheritDoc} */
        @Override
        public int compareTo(Version o) {
            return 0;
        }
    }
}
