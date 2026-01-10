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
package internal.app.packed.bean.scanning;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.function.Function;

import app.packed.bean.BeanSourceKind;
import app.packed.lifecycle.Inject;
import app.packed.operation.OperationType;
import internal.app.packed.lifecycle.LifecycleOperationHandle.FactoryOperationHandle;
import internal.app.packed.operation.OperationMemberTarget.OperationConstructorTarget;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.PackedOperationInstaller;
import internal.app.packed.operation.PackedOperationTemplate;
import internal.app.packed.util.StringFormatter;

/**
 * Tries to find a single static method or constructor on the specified class using the following rules:
 * <ul>
 * <li>If a single static method (non-static methods are ignored) annotated with {@link Inject} is present a factory
 * wrapping the method will be returned. If there are multiple static methods annotated with Inject this method will
 * fail with {@link IllegalStateException}.</li>
 * <li>If a single constructor annotated with {@link Inject} is present a factory wrapping the constructor will be
 * returned. If there are multiple constructors annotated with Inject this method will fail with
 * {@link IllegalStateException}.</li>
 * <li>If there is exactly one public constructor, a factory wrapping the constructor will be returned. If there are
 * multiple public constructors this method will fail with {@link IllegalStateException}.</li>
 * <li>If there is exactly one protected constructor, a factory wrapping the constructor will be returned. If there are
 * multiple protected constructors this method will fail with {@link IllegalStateException}.</li>
 * <li>If there is exactly one package-private constructor, a factory wrapping the constructor will be returned. If
 * there are multiple package-private constructors this method will fail with {@link IllegalStateException}.</li>
 * <li>If there is exactly one private constructor, a factory wrapping the constructor will be returned. Otherwise an
 * {@link IllegalStateException} is thrown.</li>
 * </ul>
 * <p>
 *
 * @param <T>
 *            the implementation type
 * @param implementation
 *            the implementation type
 * @return a factory for the specified type
 */
final record BeanScannerOnConstructors(Constructor<?> constructor, OperationType operationType) {

    // Probably want to use some some reference shit.
    // This will stay around forever
    // Maybe we need a wirelet that says CacheAsMuchAsPossible we will be building a lot of application

    /** A cache of constructor. */
    static final ClassValue<BeanScannerOnConstructors> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        protected BeanScannerOnConstructors computeValue(Class<?> implementation) {
            Constructor<?> executable = BeanScannerOnConstructors.getConstructor(implementation, true, e -> new IllegalArgumentException(e));
            return new BeanScannerOnConstructors(executable, OperationType.fromExecutable(executable));
        }
    };

    /**
     * @param clazz
     *            the class to scan
     * @param allowInjectAnnotation
     *            whether or not we allow usage of {@link Inject}. If not, the specified class must have a single
     *            constructor
     * @param errorMaker
     *            invoked with an error message if something goes wrong
     * @return the constructor
     */
    // Taenker vi skal have en exception der specifikt naevner noget med constructor
    // NoConstructorExtension
    // InjectableConstructorMissingException
    // MissingInjectableConstructorException
    // ConstructorInjectionException (lyder mere som noget vi ville smide naar vi instantiere det
    private static Constructor<?> getConstructor(Class<?> clazz, boolean allowInjectAnnotation, Function<String, RuntimeException> errorMaker) {
        if (clazz.isAnnotation()) { // must be checked before isInterface
            String errorMsg = StringFormatter.format(clazz) + " is an annotation and cannot be instantiated";
            throw errorMaker.apply(errorMsg);
        } else if (clazz.isInterface()) {
            String errorMsg = StringFormatter.format(clazz) + " is an interface and cannot be instantiated";
            throw errorMaker.apply(errorMsg);
        } else if (clazz.isArray()) {
            String errorMsg = StringFormatter.format(clazz) + " is an array and cannot be instantiated";
            throw errorMaker.apply(errorMsg);
        } else if (clazz.isPrimitive()) {
            String errorMsg = StringFormatter.format(clazz) + " is a primitive class and cannot be instantiated";
            throw errorMaker.apply(errorMsg);
        } else if (Modifier.isAbstract(clazz.getModifiers())) {
            String errorMsg = StringFormatter.format(clazz) + " is an abstract class and cannot be instantiated";
            throw errorMaker.apply(errorMsg);
        }

        // Get all declared constructors
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        // If we only have 1 constructor, return it.
        if (constructors.length == 1) {
            return constructors[0];
        }

        // See if we have a single constructor annotated with @Inject
        Constructor<?> constructor = null;
        for (Constructor<?> c : constructors) {
            if (c.isAnnotationPresent(Inject.class)) {
                if (constructor != null) {
                    String errorMsg = "Multiple constructors annotated with @" + Inject.class.getSimpleName() + " on class " + StringFormatter.format(clazz);
                    throw errorMaker.apply(errorMsg);
                }
                constructor = c;
            }
        }
        if (constructor != null) {
            return constructor;
        }

        // See if we have a single public constructor
        for (Constructor<?> c : constructors) {
            if (Modifier.isPublic(c.getModifiers())) {
                if (constructor != null) {
                    throw getErrMsg(clazz, "public", errorMaker);
                }
                constructor = c;
            }
        }
        if (constructor != null) {
            return constructor;
        }

        // See if we have a single protected constructor
        for (Constructor<?> c : constructors) {
            if (Modifier.isProtected(c.getModifiers())) {
                if (constructor != null) {
                    throw getErrMsg(clazz, "protected", errorMaker);
                }
                constructor = c;
            }
        }
        if (constructor != null) {
            return constructor;
        }

        // Remaining constructors are either private or package private constructors
        for (Constructor<?> c : constructors) {
            if (!Modifier.isPrivate(c.getModifiers())) {
                if (constructor != null) {
                    throw getErrMsg(clazz, "package-private", errorMaker);
                }
                constructor = c;
            }
        }
        if (constructor != null) {
            return constructor;
        }

        // Only private constructors left, and we have already checked whether or not we only have a single method
        // So we must have more than 1 private methods
        throw getErrMsg(clazz, "private", errorMaker);
    }

    private static RuntimeException getErrMsg(Class<?> type, String visibility, Function<String, RuntimeException> errorMaker) {
        String errorMsg = "No constructor annotated with @" + Inject.class.getSimpleName() + ". And multiple " + visibility + " constructors on class "
                + StringFormatter.format(type);
        return errorMaker.apply(errorMsg);
    }

    /** Find a constructor on the bean and create an operation for it. */
    static void findConstructor(BeanScanner scanner, Class<?> beanClass) {
        // If a we have a (instantiating) class source, we need to find a constructor we can use
        if (scanner.bean.bean.beanSourceKind == BeanSourceKind.CLASS) {

            BeanScannerOnConstructors constructor = BeanScannerOnConstructors.CACHE.get(beanClass);

            // Get the Constructor
            Constructor<?> con = constructor.constructor();

            // Extract a direct method handle from the constructor
            MethodHandle mh = scanner.unreflectConstructor(con);

            PackedOperationTemplate ot = scanner.bean.template.initializationTemplate();

            ot = ot.withReturnType(beanClass);

            PackedOperationInstaller installer = ot.newInstaller(constructor.operationType(), scanner.bean, scanner.bean.installedBy);

            OperationSetup os = installer.newOperationFromMember(new OperationConstructorTarget(constructor.constructor()), mh,
                    i -> new FactoryOperationHandle(i));

            // scanner.bean.operations.addLifecycleHandle((BeanLifecycleOperationHandle) os.handle());
            scanner.resolveBindings(os);
        }

    }
}
