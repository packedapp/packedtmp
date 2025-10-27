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
package app.packed;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.util.Set;

/** This class contains information about the framework. */
// Was moved from app.packed.framework
public final class Framework {
//
//    public static void main(String[] args) {
//        Package p = FrameworkProps.class.getPackage();
//        System.out.printf("%s%n  Title: %s%n  Version: %s%n  Vendor: %s%n", FrameworkProps.class.getName(), p.getImplementationTitle(),
//                p.getImplementationVersion(), p.getImplementationVendor());
//    }

    /** No framework for you. */
    private Framework() {}

    /** {@return a set of names of all the modules that make up the framework.} */
    public static Set<String> moduleNames() {
        return FrameworkNames.ALL_FRAMEWORK_MODULES;
    }

    /** {@return the name of the framework.} */
    // Idea was to be able to change the name of the framework.
    // But then we cannot have string constants with the name in it...
    public static String name() {
        return FrameworkNames.FRAMEWORK;
    }

    /** {@return the current version of the framework.} */
    public static Framework.Version version() {
        throw new UnsupportedOperationException();
    }

    // Alternative to using module declarations
    // Typically called from a static init of the class

    // Tror vi skal have dem mere specifikke fx  Bean.openClass
    @Deprecated
    public static void openClassToFramework(MethodHandles.Lookup lookup) {
        openClassToFramework(lookup, lookup.lookupClass());
    }

    public static void openClassToFramework(MethodHandles.Lookup lookup, Class<?> clazz) {}

    /** Represents a feature release of the framework. */
    public enum Release {

        /** The first feature release of the framework. */
        RELEASE_1;
    }

    /** Represents a version of the framework. */
    public static final class Version implements Comparable<Version> {
        // value should give us equals+hashCode

        /** The feature release part of the version. */
        private final Release release = Release.RELEASE_1;

        /** {@inheritDoc} */
        @Override
        public int compareTo(Version other) {
            requireNonNull(other, "other is null");
            if (release != other.release) {
                return release.ordinal() - other.release.ordinal();
            }
            return 0;
        }

        /** {@return whether or not this version represents a pre-release.} */
        public boolean isPreRelease() {
            return true;
        }

        /** {@return the feature release represented by this version.} */
        public Release release() {
            return release;
        }

        public static Version parse(String versionString) {
            requireNonNull(versionString, "versionString is null");
            throw new UnsupportedOperationException();
        }
    }
}
