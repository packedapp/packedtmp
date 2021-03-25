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

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.base.Key;
import app.packed.container.Extension;
import app.packed.container.Extension.Subtension;
import app.packed.container.InternalExtensionException;
import packed.internal.invoke.Infuser;
import packed.internal.util.ClassUtil;

/** A model for a {@link Extension.Subtension}. Not used outside of this package. */
final class SubtensionModel {

    /** Models of all subtensions. */
    private final static ClassValue<SubtensionModel> MODELS = new ClassValue<>() {

        /** {@inheritDoc} */
        @Override
        protected SubtensionModel computeValue(Class<?> type) {
            Class<? extends Subtension> subtensionClass = ClassUtil.checkProperSubclass(Subtension.class, type);

            // Check that the subtension have an extension as declaring class
            Class<?> declaringClass = subtensionClass.getDeclaringClass();
            if (declaringClass == null || !Extension.class.isAssignableFrom(declaringClass)) {
                throw new InternalExtensionException(subtensionClass
                        + " must have an Extension subclass as its declaring class, declaring class was [declaringClass = " + declaringClass + "]");
            }

            @SuppressWarnings("unchecked")
            Class<? extends Extension> extensionClass = (Class<? extends Extension>) declaringClass;
            ExtensionModel.of(extensionClass); // Check that the extension of the subtension is valid

            // Create an infuser exposing two services:
            // 1. An instance of the extension that the subtension is a part of
            // 2. The class of the extension that wants to use the subtension
            Infuser infuser = Infuser.build(MethodHandles.lookup(), c -> {
                c.provide(extensionClass).adapt(); // Extension instance of the subtension
                c.provide(new Key<Class<? extends Extension>>() {}).adapt(1); // Requesting extension
            }, Extension.class, Class.class);

            // Find a method handle for the subtensions's constructor
            MethodHandle constructor = infuser.singleConstructor(subtensionClass, Subtension.class, m -> new InternalExtensionException(m));
            
            return new SubtensionModel(extensionClass, constructor);
        }
    };

    /** The declaring extension class. */
    final Class<? extends Extension> extensionClass;

    /** The constructor of the subtension that we model. */
    private final MethodHandle mhConstructor; // (Extension,Class)Subtension

    /**
     * Create a new model.
     * 
     * @param extensionClass
     *            the declaring extension class
     * @param mhConstructor
     *            a constructor for the subtension
     */
    private SubtensionModel(Class<? extends Extension> extensionClass, MethodHandle mhConstructor) {
        this.extensionClass = requireNonNull(extensionClass);
        this.mhConstructor = requireNonNull(mhConstructor);
    }

    /**
     * Creates a new subtension instance.
     * 
     * @param extension
     *            an instance of the declaring extension class
     * @param requestingExtensionClass
     *            the extension that is requesting an instance
     * @return the new subtension instance
     */
    Subtension newInstance(Extension extension, Class<? extends Extension> requestingExtensionClass) {
        try {
            return (Subtension) mhConstructor.invokeExact(extension, requestingExtensionClass);
        } catch (Throwable e) {
            throw new InternalExtensionException("Instantiation of " + Subtension.class + " failed", e);
        }
    }

    /**
     * Returns a model from the specified subtension class.
     * 
     * @param subtensionClass
     *            the subtension class
     * @return a model for the subtension class
     */
    static SubtensionModel of(Class<? extends Extension.Subtension> subtensionClass) {
        return MODELS.get(subtensionClass);
    }
}
