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
import java.lang.reflect.Type;

import app.packed.container.Extension;
import app.packed.container.ExtensionMirror;
import packed.internal.util.ClassUtil;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;
import packed.internal.util.typevariable.TypeVariableExtractor;

record ExtensionMirrorModel(Class<? extends Extension<?>> extensionType) {

    /** A handle for invoking the protected method {@link Extension#newExtensionMirror()}. */
    private static final MethodHandle MH_EXTENSION_MIRROR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ExtensionMirror.class,
            "initialize", void.class, PackedExtensionTree.class);

    /** A type variable extractor. */
    private static final TypeVariableExtractor TYPE_LITERAL_EP_EXTRACTOR = TypeVariableExtractor.of(ExtensionMirror.class);

    /** Models of all subtensions. */
    private final static ClassValue<ExtensionMirrorModel> MODELS = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        protected ExtensionMirrorModel computeValue(Class<?> type) {
            ClassUtil.checkProperSubclass((Class) ExtensionMirror.class, type);

            Type t = TYPE_LITERAL_EP_EXTRACTOR.extract(type);
//            System.out.println(t);
//            // Check that the subtension have an extension as declaring class
//            ExtensionMember extensionMember = subtensionClass.getAnnotation(ExtensionMember.class);
//            if (extensionMember == null) {
//                throw new InternalExtensionException(subtensionClass + " must be annotated with @ExtensionMember");
//            }

            Class<? extends Extension<?>> extensionClass = (Class<? extends Extension<?>>) t;// extensionMember.value();
            // TODO check same module
            // Move to a common method and share it with mirror
            //

            ExtensionModel.of(extensionClass); // Check that the extension is valid
            return new ExtensionMirrorModel(extensionClass);
        }
    };

    /**
     * Returns a model from the specified subtension class.
     * 
     * @param mirrorClass
     *            the subtension class
     * @return a model for the subtension class
     */
    static ExtensionMirrorModel of(Class<?> mirrorClass) {
        return MODELS.get(mirrorClass);
    }

    /**
     * @param mirror
     * @param extensionSetup
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void initialize(ExtensionMirror<?> mirror, ExtensionSetup extensionSetup) {
        try {
            MH_EXTENSION_MIRROR_INITIALIZE.invokeExact(mirror, new PackedExtensionTree(extensionSetup, extensionSetup.extensionType));
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }
}
