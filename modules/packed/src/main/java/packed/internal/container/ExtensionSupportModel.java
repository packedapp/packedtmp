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
package packed.internal.container;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.extension.Extension;
import app.packed.extension.ExtensionMember;
import app.packed.extension.ExtensionPoint;
import app.packed.extension.ExtensionPointContext;
import app.packed.extension.InternalExtensionException;
import packed.internal.inject.invoke.InternalInfuser;
import packed.internal.util.ClassUtil;

/** A model for an {@link Extension.ExtensionPoint} class. Not used outside of this package. */
record ExtensionSupportModel(Class<? extends Extension<?>> extensionType, MethodHandle mhConstructor) {

    /** Models of all subtensions. */
    private final static ClassValue<ExtensionSupportModel> MODELS = new ClassValue<>() {

        /** {@inheritDoc} */
        @Override
        protected ExtensionSupportModel computeValue(Class<?> type) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            Class<? extends ExtensionPoint<?>> subtensionClass = ClassUtil.checkProperSubclass((Class) ExtensionPoint.class, type);

            // Check that the subtension have an extension as declaring class
            ExtensionMember extensionMember = subtensionClass.getAnnotation(ExtensionMember.class);
            if (extensionMember == null) {
                throw new InternalExtensionException(subtensionClass + " must be annotated with @ExtensionMember");
            }

            // TODO check same module
            // Move to a common method
            //
//            if (declaringClass == null || !ExtensionSupport.class.isAssignableFrom(declaringClass)) {
//                throw new InternalExtensionException(subtensionClass
//                        + " must have an Extension subclass as its declaring class, declaring class was [declaringClass = " + declaringClass + "]");
//            }

            Class<? extends Extension<?>> extensionClass = extensionMember.value();
            ExtensionModel.of(extensionClass); // Check that the extension of the subtension is valid

            // Create an infuser exposing two services:
            // 1. An instance of the extension that the subtension is a part of
            // 2. The class of the extension that wants to use the subtension
            InternalInfuser.Builder builder = InternalInfuser.builder(MethodHandles.lookup(), subtensionClass, Extension.class, ExtensionPointContext.class);
            builder.provide(extensionClass).adaptArgument(0); // Extension instance of the subtension
            builder.provide(ExtensionPointContext.class).adaptArgument(1); // Extension instance of the subtension

            // Find a method handle for the subtensions's constructor
            MethodHandle constructor = builder.findConstructor(ExtensionPoint.class, m -> new InternalExtensionException(m));

            return new ExtensionSupportModel(extensionClass, constructor);
        }
    };

    /**
     * Creates a new extension support class instance.
     * 
     * @param extension
     *            an instance of the declaring extension class
     * @param requestingExtensionClass
     *            the extension that is requesting an instance
     * @return the new subtension instance
     */
    ExtensionPoint<?> newInstance(Extension<?> extension, ExtensionPointContext context) {
        // mhConstructor = (Extension,ExtensionSupportContext)Subtension
        try {
            return (ExtensionPoint<?>) mhConstructor.invokeExact(extension, context);
        } catch (Throwable e) {
            throw new InternalExtensionException("Instantiation of " + ExtensionPoint.class + " failed", e);
        }
    }

    /**
     * Returns a model from the specified subtension class.
     * 
     * @param subtensionClass
     *            the subtension class
     * @return a model for the subtension class
     */
    static ExtensionSupportModel of(Class<? extends ExtensionPoint<?>> subtensionClass) {
        return MODELS.get(subtensionClass);
    }
}
