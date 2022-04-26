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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import app.packed.base.Key;
import app.packed.base.TypeToken;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionMirror;
import app.packed.extension.ExtensionPoint;
import app.packed.extension.ExtensionTree;
import app.packed.extension.InternalExtensionException;
import packed.internal.inject.invoke.InternalInfuser;
import packed.internal.thirdparty.guice.GTypes;
import packed.internal.util.ClassUtil;
import packed.internal.util.LookupUtil;
import packed.internal.util.typevariable.TypeVariableExtractor;

/** A model for an {@link Extension.ExtensionPoint} class. Not used outside of this package. */
record ExtensionMirrorModel(Class<? extends Extension<?>> extensionType, MethodHandle mhConstructor) {

    /** A handle for setting the private field Extension#context. */
    private static final VarHandle VH_EXTENSION_MIRROR_TREE = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), ExtensionMirror.class, "tree",
            PackedExtensionTree.class);

    /** A handle for setting the private field Extension#context. */
    private static final VarHandle VH_EXTENSION_MIRROR_EXTENSION = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), ExtensionMirror.class, "extension",
            ExtensionSetup.class);

    /** A type variable extractor. */
    private static final TypeVariableExtractor TYPE_LITERAL_EP_EXTRACTOR = TypeVariableExtractor.of(ExtensionMirror.class);

    /** Models of all subtensions. */
    private final static ClassValue<ExtensionMirrorModel> MODELS = new ClassValue<>() {

        /** {@inheritDoc} */
        @Override
        protected ExtensionMirrorModel computeValue(Class<?> type) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            Class<? extends ExtensionPoint<?>> mirrorClass = ClassUtil.checkProperSubclass((Class) ExtensionPoint.class, type);

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

            // ExtensionTree<BeanExtension>

            // Create an infuser exposing two services:
            // 1. An instance of the extension that the extension point is a member of
            // 2. An ExtensionPoint.UseSite instance
            InternalInfuser.Builder builder = InternalInfuser.builder(MethodHandles.lookup(), mirrorClass, Extension.class, ExtensionTree.class);
            builder.provide(extensionClass).adaptArgument(0); // Extension instance of the subtension

            ParameterizedType pt = GTypes.newParameterizedType(ExtensionTree.class, extensionClass);
            TypeToken<?> tt = TypeToken.fromType(pt);
            builder.provide(Key.ofTypeToken(tt)).adaptArgument(1);

            // Find a method handle for the subtensions's constructor
            MethodHandle constructor = builder.findConstructor(ExtensionMirror.class, m -> new InternalExtensionException(m));

            return new ExtensionMirrorModel(extensionClass, constructor);
        }
    };

    /**
     * Creates a new extension support class instance.
     * 
     * @param otherExtension
     *            an instance of the declaring extension class
     * @param requestingExtensionClass
     *            the extension that is requesting an instance
     * @return the new subtension instance
     */
    ExtensionMirror<?> newInstance(ExtensionSetup extension, PackedExtensionTree<?> extensionTree) {
        // mhConstructor = (Extension,ExtensionSupportContext)Subtension
        try {
            ExtensionMirror<?> m = (ExtensionMirror<?>) mhConstructor.invokeExact(extension.instance(), (ExtensionTree<?>) extensionTree);
            VH_EXTENSION_MIRROR_TREE.set(m, extensionTree);
            VH_EXTENSION_MIRROR_EXTENSION.set(m, extension);
            return m;
        } catch (Throwable e) {
            throw new InternalExtensionException("Instantiation of " + ExtensionMirror.class + " failed", e);
        }
    }

    /**
     * Returns a model from the specified subtension class.
     * 
     * @param subtensionClass
     *            the subtension class
     * @return a model for the subtension class
     */
    static ExtensionMirrorModel of(Class<? extends ExtensionMirror<?>> subtensionClass) {
        return MODELS.get(subtensionClass);
    }
}
