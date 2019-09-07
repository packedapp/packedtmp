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
    private final static ConcurrentHashMap<Class<? extends SecretAccess>, SecretAccess> TMP = new ConcurrentHashMap<>();

    /** Never instantiate. */
    private SharedSecrets() {}

    /**
     * Returns an access object that can access methods in app.packed.artifact.
     * 
     * @return an access object that can access methods in app.packed.artifact
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

    /**
     * Initializes an access object.
     * 
     * @param <T>
     *            the type of access
     * @param accessType
     *            the type of access
     * @param access
     *            the access object
     */
    public static <T extends SecretAccess> void initialize(Class<T> accessType, T access) {
        if (TMP.putIfAbsent(accessType, access) != null) {
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

    private static <T extends SecretAccess> T singleton(Class<T> accessType, Class<?> initalizeClass) {
        // Start by making sure the class is initialized
        try {
            Class.forName(initalizeClass.getName(), true, initalizeClass.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e); // Should never happen
        }

        SecretAccess access = TMP.get(accessType);
        if (access == null) {
            throw new ExceptionInInitializerError("An instance of " + accessType + " has not been set");
        }
        return accessType.cast(access);
    }

    /**
     * Returns an access object for app.packed.util.
     * 
     * @return an access object for app.packed.util
     */
    public static AppPackedUtilAccess util() {
        return UtilSingletonHolder.SINGLETON;
    }

    /** Singleton holder for {@link AppPackedArtifactAccess}. */
    private static class ArtifactSingletonHolder {

        /** The singleton instance. */
        private static final AppPackedArtifactAccess SINGLETON = singleton(AppPackedArtifactAccess.class, ArtifactImage.class);
    }

    /** Holder of the {@link AppPackedContainerAccess} singleton. */
    private static class ContainerSingletonHolder {

        /** The singleton instance. */
        private static final AppPackedContainerAccess SINGLETON = singleton(AppPackedContainerAccess.class, WireletList.class);
    }

    /** Holder of the {@link AppPackedExtensionAccess} singleton. */
    private static class ExtensionSingletonHolder {

        /** The singleton instance. */
        private static final AppPackedExtensionAccess SINGLETON = singleton(AppPackedExtensionAccess.class, Extension.class);
    }

    /** Holder of the {@link AppPackedInjectAccess} singleton. */
    private static class InjectSingletonHolder {

        /** The singleton instance. */
        private static final AppPackedInjectAccess SINGLETON = singleton(AppPackedInjectAccess.class, Factory.class);
    }

    /** Holder of the {@link AppPackedLifecycleAccess} singleton. */
    private static class LifecycleSingletonHolder {

        /** The singleton instance. */
        private static final AppPackedLifecycleAccess SINGLETON = singleton(AppPackedLifecycleAccess.class, RunState.class);
    }

    /** Holder of the {@link AppPackedUtilAccess} singleton. */
    private static class UtilSingletonHolder {

        /** The singleton instance. */
        private static final AppPackedUtilAccess SINGLETON = singleton(AppPackedUtilAccess.class, TypeLiteral.class);
    }
}
