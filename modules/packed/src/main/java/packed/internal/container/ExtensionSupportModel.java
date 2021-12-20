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

import app.packed.base.Key;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionMember;
import app.packed.extension.ExtensionSupport;
import app.packed.extension.InternalExtensionException;
import packed.internal.invoke.Infuser;
import packed.internal.util.ClassUtil;

/** A model for a {@link Extension.ExtensionSupport} class. Not used outside of this package. */
record ExtensionSupportModel(Class<? extends Extension> extensionType, MethodHandle mhConstructor) {

    /** Models of all subtensions. */
    private final static ClassValue<ExtensionSupportModel> MODELS = new ClassValue<>() {

        /** {@inheritDoc} */
        @Override
        protected ExtensionSupportModel computeValue(Class<?> type) {
            Class<? extends ExtensionSupport> subtensionClass = ClassUtil.checkProperSubclass(ExtensionSupport.class, type);

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

            Class<? extends Extension> extensionClass = extensionMember.value();
            ExtensionModel.of(extensionClass); // Check that the extension of the subtension is valid

            // Create an infuser exposing two services:
            // 1. An instance of the extension that the subtension is a part of
            // 2. The class of the extension that wants to use the subtension
            Infuser.Builder builder = Infuser.builder(MethodHandles.lookup(), subtensionClass, Extension.class, Class.class);
            builder.provide(extensionClass).adaptArgument(0); // Extension instance of the subtension
            builder.provide(new Key<Class<? extends Extension>>() {}).adaptArgument(1); // Requesting extension

            // Find a method handle for the subtensions's constructor
            MethodHandle constructor = builder.findConstructor(ExtensionSupport.class, m -> new InternalExtensionException(m));

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
    ExtensionSupport newInstance(Extension extension, Class<? extends Extension> requestingExtensionClass) {
        // mhConstructor = (Extension,Class)Subtension
        try {
            return (ExtensionSupport) mhConstructor.invokeExact(extension, requestingExtensionClass);
        } catch (Throwable e) {
            throw new InternalExtensionException("Instantiation of " + ExtensionSupport.class + " failed", e);
        }
    }

    /**
     * Returns a model from the specified subtension class.
     * 
     * @param subtensionClass
     *            the subtension class
     * @return a model for the subtension class
     */
    static ExtensionSupportModel of(Class<? extends ExtensionSupport> subtensionClass) {
        return MODELS.get(subtensionClass);
    }
}
