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
import packed.internal.inject.classscan.Infuser;
import packed.internal.util.MethodHandleUtil;

/** A model of a subclass of {@link Extension.Subtension}. Not used outside of this package. */
final class SubtensionModel {

    /** Models of all subtensions. */
    private final static ClassValue<SubtensionModel> MODELS = new ClassValue<>() {

        /** {@inheritDoc} */
        @Override
        protected SubtensionModel computeValue(Class<?> subtensionClass) {
            // Extension havr called extension.use(Extension.Subtension.class)
            if (subtensionClass == Extension.Subtension.class) {
                throw new IllegalArgumentException("Cannot specify " + Extension.Subtension.class.getCanonicalName());
            }
            Class<?> declaringClass = subtensionClass.getDeclaringClass();
            if (declaringClass == null || !Extension.class.isAssignableFrom(declaringClass)) {
                throw new InternalExtensionException(
                        subtensionClass + " must have an Extension subclass as its declaring class, declaring class was [declaringClass = " + declaringClass + "]");
            }

            @SuppressWarnings("unchecked")
            Class<? extends Extension> extensionClass = (Class<? extends Extension>) declaringClass;
            ExtensionModel.of(extensionClass); // make sure the extension is valid

            // Create an infuser (SomeExtension, Class)
            Infuser infuser = Infuser.build(MethodHandles.lookup(), c -> {
                c.expose(extensionClass).adapt(0); // The extension the Subtension belongs
                c.expose(new Key<Class<? extends Extension>>() {}).adapt(1); // The requesting extension
            }, extensionClass, Class.class);

            // Find the subtension constructor using the infuser
            MethodHandle constructor = infuser.findConstructorFor(subtensionClass);// MethodHandle(SomeExtension,Class)SomeSubtension
            
            // We want to have the signature MethodHandle(Extension,Class)Subtension
            // so we can use invokeExact in newInstance method
            constructor = constructor.asType(constructor.type().changeParameterType(0, Extension.class));
            constructor = MethodHandleUtil.castReturnType(constructor, Subtension.class);

            return new SubtensionModel(extensionClass, constructor);
        }
    };

    /** The constructor of the subtension that we model. */
    private final MethodHandle constructor;

    /** The declaring extension. */
    final Class<? extends Extension> extensionClass;

    /**
     * Creates a new model.
     * 
     * @param extensionClass
     *            the declaring extension type
     * @param constructor
     *            a constructor for the subtensions
     */
    private SubtensionModel(Class<? extends Extension> extensionClass, MethodHandle constructor) {
        this.extensionClass = requireNonNull(extensionClass);
        this.constructor = requireNonNull(constructor);
    }

    /**
     * Creates a new subtension instance
     * 
     * @param extension
     *            an instance of the declaring extension type
     * @param requestor
     *            the extension that requested an instance
     * @return the new subtension instance
     */
    Subtension newInstance(Extension extension, Class<? extends Extension> requestor) {
        try {
            return (Subtension) constructor.invokeExact(extension, requestor);
        } catch (Throwable e) {
            throw new InternalExtensionException("Instantiation of " + Subtension.class.getSimpleName() + " failed", e);
        }
    }

    /**
     * Returns a model from the specified subtension subclass.
     * 
     * @param subType
     *            the subtension subclass
     * @return
     */
    static SubtensionModel of(Class<? extends Extension.Subtension> subType) {
        return MODELS.get(subType);
    }
}
