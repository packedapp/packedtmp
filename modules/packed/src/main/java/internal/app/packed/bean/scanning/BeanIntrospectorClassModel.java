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
import java.lang.reflect.Type;

import app.packed.bean.scanning.BeanIntrospector;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionDescriptor;
import app.packed.extension.InternalExtensionException;
import app.packed.util.Nullable;
import internal.app.packed.extension.ExtensionClassModel;
import internal.app.packed.invoke.ExtensionLookupSupport;
import internal.app.packed.util.types.ClassUtil;
import internal.app.packed.util.types.TypeVariableExtractor;

/** A class model for a {@link BeanIntrospector} implementation. */
final class BeanIntrospectorClassModel {

    /** A cache of all encountered extension models. */
    private static final ClassValue<BeanIntrospectorClassModel> MODELS = new ClassValue<>() {

        /** A type variable extractor. */
        private static final TypeVariableExtractor EXTRACTOR = TypeVariableExtractor.of(BeanIntrospector.class);

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked" })
        @Override
        protected BeanIntrospectorClassModel computeValue(Class<?> beanInspectorClass) {
            // new Exception().printStackTrace();
//            ClassUtil.checkProperSubclass(Extension.class, extensionClass, s -> new InternalExtensionException(s));
//            // Check that framework extensions are in a framework module
//            if (FrameworkExtension.class.isAssignableFrom(extensionClass)) {
//                Module m = extensionClass.getModule();
//                if (m.isNamed() && !Framework.moduleNames().contains(m.getName())) {
//                    throw new InternalExtensionException("Extension " + extensionClass + " extends " + FrameworkExtension.class
//                            + " but is not located in module(s) " + Framework.moduleNames() + " or the unnamed module");
//                }
//            }

            Class<? extends Extension<?>> e = ExtensionClassModel.extractE(EXTRACTOR, beanInspectorClass);
            MethodHandle mh = ExtensionLookupSupport.findBeanIntrospector((Class<? extends BeanIntrospector<?>>) beanInspectorClass);
            return new BeanIntrospectorClassModel(e, mh);
        }
    };

    /** A method handle for creating new instances of extensionClass. */
    private final MethodHandle mhConstructor; // (ExtensionSetup)Extension

    final Class<? extends Extension<?>> extensionClass;

    /**
     * Creates a new extension model from the specified builder.
     *
     * @param builder
     *            the builder of the model
     */
    private BeanIntrospectorClassModel(Class<? extends Extension<?>> extensionClass, MethodHandle mhConstructor) {
        this.mhConstructor = mhConstructor;
        this.extensionClass = extensionClass;
    }

//    @SuppressWarnings("unchecked")
//    public Class<? extends BeanIntrospector<?>> beanIntrospectorClass() {
//        return (Class<? extends BeanIntrospector<?>>) mhConstructor.type().returnType();
//    }

    /**
     * Returns any value of nest annotation.
     *
     * @param eType
     *            the type look for an ExtensionMember annotation on
     * @return an extension the specified type is a member of
     * @throws InternalExtensionException
     *             if an annotation is present and the specified is not in the same module as the extension specified in
     *             next
     */
    @Nullable
    public Class<? extends Extension<?>> checkSameModule(Class<? extends Extension<?>> eType) {
//        if (extensionClass.getModule() != eType.getModule()) {
//            throw new InternalExtensionException("The extension " + eType + " and type " + extensionClass + " must be defined in the same module, was "
//                    + eType.getModule() + " and " + extensionClass.getModule());
//        }
        // of(eType); // Make sure a valid model for the extension has been created
        return eType;
    }

    public static Class<? extends Extension<?>> extractE(TypeVariableExtractor tve, Class<?> type) {
        // Extract the type of extension from ExtensionMirror<E>
        Type t = tve.extractType(type, InternalExtensionException::new);

        @SuppressWarnings("unchecked")
        Class<? extends Extension<?>> extensionClass = (Class<? extends Extension<?>>) t; //

        // Check that we are a proper subclass of
        ClassUtil.checkProperSubclass(Extension.class, extensionClass, InternalExtensionException::new);

        // Check that the mirror is in the same module as the extension itself
        if (extensionClass.getModule() != type.getModule()) {
            throw new InternalExtensionException("The extension support class " + type + " must in the same module (" + extensionClass.getModule() + ") as "
                    + extensionClass + ", but was in '" + type.getModule() + "'");
        }

        return ExtensionDescriptor.of(extensionClass).type(); // Check that the extension is valid
    }

    /**
     * Creates a new instance of the extension.
     *
     * @param extension
     *            the setup of the extension
     * @return a new extension instance
     */
    public BeanIntrospector<?> newInstance() {
        try {
            return (BeanIntrospector<?>) mhConstructor.invokeExact();
        } catch (Throwable e) {
            throw new InternalExtensionException("An instance of " + mhConstructor.type().returnType() + " could not be created.", e);
        }
    }

    /**
     * Returns an model for the specified bean introspector type.
     *
     * @param beanIntrospectorType
     *            the bean introspector type to return a model for
     * @return an bean introspector model for the specified bean introspector type
     * @throws InternalExtensionException
     *             if a valid model for the bean introspector could not be created
     */
    public static BeanIntrospectorClassModel of(Class<? extends BeanIntrospector<?>> beanIntrospectorType) {
        return MODELS.get(beanIntrospectorType);
    }
}
