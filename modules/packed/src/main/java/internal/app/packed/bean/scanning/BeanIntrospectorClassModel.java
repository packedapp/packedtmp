/*
   * Copyright (c) 2026 Kasper Nielsen.
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

import java.lang.reflect.Type;

import app.packed.bean.BeanIntrospector;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionDescriptor;
import app.packed.extension.InternalExtensionException;
import org.jspecify.annotations.Nullable;
import internal.app.packed.extension.ExtensionClassModel;
import internal.app.packed.invoke.ConstructorSupport;
import internal.app.packed.invoke.ConstructorSupport.BeanIntrospectorFactory;
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
            Class<? extends Extension<?>> e = ExtensionClassModel.extractE(EXTRACTOR, beanInspectorClass);
            BeanIntrospectorFactory factory = ConstructorSupport.findBeanIntrospector((Class<? extends BeanIntrospector<?>>) beanInspectorClass);
            return new BeanIntrospectorClassModel(e, factory);
        }
    };

    /** A factory for creating new instances of the BeanIntrospector. */
    private final BeanIntrospectorFactory factory;

    final Class<? extends Extension<?>> extensionClass;

    /**
     * Creates a new extension model from the specified builder.
     *
     * @param builder
     *            the builder of the model
     */
    private BeanIntrospectorClassModel(Class<? extends Extension<?>> extensionClass, BeanIntrospectorFactory factory) {
        this.factory = factory;
        this.extensionClass = extensionClass;
    }

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
     * Creates a new instance of the BeanIntrospector.
     *
     * @return a new BeanIntrospector instance
     */
    public BeanIntrospector<?> newInstance() {
        return factory.create();
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
