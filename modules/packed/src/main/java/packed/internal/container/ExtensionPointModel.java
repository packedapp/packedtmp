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
import java.lang.invoke.VarHandle;
import java.lang.reflect.Type;

import app.packed.extension.Extension;
import app.packed.extension.ExtensionPoint;
import app.packed.extension.ExtensionPoint.UseSite;
import app.packed.extension.InternalExtensionException;
import packed.internal.inject.invoke.InternalInfuser;
import packed.internal.util.ClassUtil;
import packed.internal.util.LookupUtil;
import packed.internal.util.typevariable.TypeVariableExtractor;

/** A model for an {@link Extension.ExtensionPoint} class. Not used outside of this package. */
record ExtensionPointModel(Class<? extends Extension<?>> extensionType, MethodHandle mhConstructor) {

    /** A handle for setting the private field Extension#context. */
    private static final VarHandle VH_EXTENSION_POINT_SETUP = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), ExtensionPoint.class, "useSite",
            PackedExtensionPointUseSite.class);

    /** A type variable extractor. */
    private static final TypeVariableExtractor TYPE_LITERAL_EP_EXTRACTOR = TypeVariableExtractor.of(ExtensionPoint.class);

    /** Models of all subtensions. */
    private final static ClassValue<ExtensionPointModel> MODELS = new ClassValue<>() {

        /** {@inheritDoc} */
        @Override
        protected ExtensionPointModel computeValue(Class<?> type) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            Class<? extends ExtensionPoint<?>> subtensionClass = ClassUtil.checkProperSubclass((Class) ExtensionPoint.class, type);

            Type t = TYPE_LITERAL_EP_EXTRACTOR.extract(type);
//            System.out.println(t);
//            // Check that the subtension have an extension as declaring class
//            ExtensionMember extensionMember = subtensionClass.getAnnotation(ExtensionMember.class);
//            if (extensionMember == null) {
//                throw new InternalExtensionException(subtensionClass + " must be annotated with @ExtensionMember");
//            }

            @SuppressWarnings("unchecked")
            Class<? extends Extension<?>> extensionClass = (Class<? extends Extension<?>>) t;// extensionMember.value();
            // TODO check same module
            // Move to a common method and share it with mirror
            //

            ExtensionModel.of(extensionClass); // Check that the extension of the subtension is valid

            // Create an infuser exposing two services:
            // 1. An instance of the extension that the extension point is a member of
            // 2. An ExtensionPoint.UseSite instance
            InternalInfuser.Builder builder = InternalInfuser.builder(MethodHandles.lookup(), subtensionClass, Extension.class, UseSite.class);
            builder.provide(extensionClass).adaptArgument(0); // Extension instance of the subtension
            builder.provide(UseSite.class).adaptArgument(1); // Extension instance of the subtension

            // Find a method handle for the subtensions's constructor
            MethodHandle constructor = builder.findConstructor(ExtensionPoint.class, m -> new InternalExtensionException(m));

            return new ExtensionPointModel(extensionClass, constructor);
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
    ExtensionPoint<?> newInstance(Extension<?> extension, PackedExtensionPointUseSite context) {
        // mhConstructor = (Extension,ExtensionSupportContext)Subtension
        try {
            ExtensionPoint<?> p = (ExtensionPoint<?>) mhConstructor.invokeExact(extension, (UseSite) context);
            VH_EXTENSION_POINT_SETUP.set(p, context);
            return p;
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
    static ExtensionPointModel of(Class<? extends ExtensionPoint<?>> subtensionClass) {
        return MODELS.get(subtensionClass);
    }
}
