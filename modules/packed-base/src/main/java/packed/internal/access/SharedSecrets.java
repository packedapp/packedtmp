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
package packed.internal.access;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.ConcurrentHashMap;

import app.packed.artifact.ArtifactImage;
import app.packed.container.WireletList;
import app.packed.container.extension.Extension;
import app.packed.inject.Factory;
import app.packed.lifecycle.RunState;
import app.packed.util.TypeLiteral;

/**
 * A collection of "shared secrets", which are a mechanism for calling package private methods in public packages
 * without using reflection.
 */
public final class SharedSecrets {

    /** All secrets, we never remove them to make sure we never add anything twice. */
    private final static ConcurrentHashMap<Class<? extends SecretAccess>, SecretAccess> MAP = new ConcurrentHashMap<>();

    /** A temporary instance of AppPackedContainerAccess, until ContainerSingletonHolder has been initialized. */
    private static AppPackedContainerAccess TMP_CONTAINER_ACCESS;

    /** A temporary instance of AppPackedExtensionAccess, until ExtensionSingletonHolder has been initialized. */
    private static AppPackedExtensionAccess TMP_EXTENSION_ACCESS;

    /** A temporary instance of AppPackedInjectAccess, until InjectSingletonHolder has been initialized. */
    private static AppPackedInjectAccess TMP_INJECT_ACCESS;

    /** A temporary instance of AppPackedLifecycleAccess, until LifecycleSingleHolder has been initialized. */
    private static AppPackedLifecycleAccess TMP_LIFECYCLE_ACCESS;

    /** A temporary instance of AppPackedUtilAccess, until UtilSingletonHolder has been initialized. */
    private static AppPackedUtilAccess TMP_UTIL_ACCESS;

    /** Never instantiate */
    private SharedSecrets() {}

    /**
     * Returns an access object for app.packed.artifact.
     * 
     * @return an access object for app.packed.artifact
     */
    public static AppPackedArtifactAccess artifact() {
        return ArtifactSingletonHolder.SINGLETON;
    }

    /**
     * Returns an access object for app.packed.container.
     * 
     * @return an access object for app.packed.container
     */
    public static AppPackedContainerAccess container() {
        return ContainerSingletonHolder.SINGLETON;
    }

    /**
     * Returns an access object for app.packed.container.extension.
     * 
     * @return an access object for app.packed.container.extension
     */
    public static AppPackedExtensionAccess extension() {
        return ExtensionSingletonHolder.SINGLETON;
    }

    static <T extends SecretAccess> T get(Class<T> accessType, Class<?> initalizeClass) {
        SecretAccess a = MAP.get(accessType);
        if (a == null) {
            throw new ExceptionInInitializerError("An instance of " + accessType + " has not been set");
        }
        try {
            Class.forName(initalizeClass.getName(), true, initalizeClass.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e); // Should not happen
        }
        return accessType.cast(a);
    }

    public static <T extends SecretAccess> void initialize(Class<T> accessType, T access) {
        if (MAP.putIfAbsent(accessType, access) != null) {
            throw new ExceptionInInitializerError("An instance of " + accessType + " has already been set ");
        }
    }

    /**
     * Returns an access object for app.packed.inject.
     * 
     * @return an access object for app.packed.inject
     */
    public static AppPackedInjectAccess inject() {
        return InjectSingletonHolder.SINGLETON;
    }

    /**
     * Returns an access object for app.packed.lifecycle.
     * 
     * @return an access object for app.packed.lifecycle
     */
    public static AppPackedLifecycleAccess lifecycle() {
        return LifecycleSingletonHolder.SINGLETON;
    }

    /**
     * Returns an access object for app.packed.util.
     * 
     * @return an access object for app.packed.util
     */
    public static AppPackedUtilAccess util() {
        return UtilSingletonHolder.SINGLETON;
    }

    /**
     * Initializes {@link AppPackedContainerAccess} support.
     * 
     * @param access
     *            the access object
     */
    public static void zet(AppPackedContainerAccess access) {
        if (TMP_CONTAINER_ACCESS != null) {
            throw new Error("Can only be initialized once");
        }
        TMP_CONTAINER_ACCESS = requireNonNull(access);
    }

    /**
     * Initializes {@link AppPackedExtensionAccess} support.
     * 
     * @param access
     *            the access object
     */
    public static void zet(AppPackedExtensionAccess access) {
        if (TMP_EXTENSION_ACCESS != null) {
            throw new Error("Can only be initialized once");
        }
        TMP_EXTENSION_ACCESS = requireNonNull(access);
    }

    /**
     * Initializes {@link AppPackedInjectAccess} support.
     * 
     * @param access
     *            the access object
     */
    public static void zet(AppPackedInjectAccess access) {
        if (TMP_INJECT_ACCESS != null) {
            throw new Error("Can only be initialized once");
        }
        TMP_INJECT_ACCESS = requireNonNull(access);
    }

    /**
     * Initializes {@link AppPackedLifecycleAccess} support.
     * 
     * @param access
     *            the access object
     */
    public static void zet(AppPackedLifecycleAccess access) {
        if (TMP_LIFECYCLE_ACCESS != null) {
            throw new Error("Can only be initialized once");
        }
        TMP_LIFECYCLE_ACCESS = requireNonNull(access);
    }

    /**
     * Initializes {@link AppPackedUtilAccess} support.
     * 
     * @param access
     *            the access object
     */
    public static void zet(AppPackedUtilAccess access) {
        if (TMP_UTIL_ACCESS != null) {
            throw new Error("Can only be initialized once");
        }
        TMP_UTIL_ACCESS = requireNonNull(access);
    }

    /** Holder of the {@link AppPackedArtifactAccess} singleton. */
    private static class ArtifactSingletonHolder {

        /** The singleton instance. */
        private static final AppPackedArtifactAccess SINGLETON = get(AppPackedArtifactAccess.class, ArtifactImage.class);
    }

    /** Holder of the {@link AppPackedContainerAccess} singleton. */
    private static class ContainerSingletonHolder {

        /** The singleton instance. */
        private static final AppPackedContainerAccess SINGLETON;

        static {
            WireletList.of(); // Forces class initialization of WireletList which invokes zet()
            SINGLETON = requireNonNull(SharedSecrets.TMP_CONTAINER_ACCESS, "internal error");
        }
    }

    /** Holder of the {@link AppPackedExtensionAccess} singleton. */
    private static class ExtensionSingletonHolder {

        /** The singleton instance. */
        private static final AppPackedExtensionAccess SINGLETON;

        static {
            // TODO try an avoid constructing a new class
            new Extension() {}; // Forces class initialization of Extension which invokes zet()
            SINGLETON = requireNonNull(SharedSecrets.TMP_EXTENSION_ACCESS, "internal error");
        }
    }

    /** Holder of the {@link AppPackedInjectAccess} singleton. */
    private static class InjectSingletonHolder {

        /** The singleton instance. */
        private static final AppPackedInjectAccess SINGLETON;

        static {
            Factory.ofInstance("foo"); // Forces class initialization of Factory which invokes zet()
            SINGLETON = requireNonNull(SharedSecrets.TMP_INJECT_ACCESS, "internal error");
        }
    }

    /** Holder of the {@link AppPackedLifecycleAccess} singleton. */
    private static class LifecycleSingletonHolder {

        /** The singleton instance. */
        private static final AppPackedLifecycleAccess SINGLETON;

        static {
            RunState.INITIALIZED.ordinal();// Forces class initialization of RunState which invokes zet()
            SINGLETON = requireNonNull(SharedSecrets.TMP_LIFECYCLE_ACCESS, "internal error");
        }
    }

    /** Holder of the {@link AppPackedUtilAccess} singleton. */
    private static class UtilSingletonHolder {

        /** The singleton instance. */
        private static final AppPackedUtilAccess SINGLETON;

        static {
            TypeLiteral.of(Object.class); // Forces class initialization of TypeLiteral, which in turn will call zet()
            SINGLETON = requireNonNull(SharedSecrets.TMP_UTIL_ACCESS, "internal error");
        }
    }
}
