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

import java.lang.reflect.Type;

import app.packed.container.Extension;
import app.packed.container.ExtensionMirror;
import packed.internal.util.typevariable.TypeVariableExtractor;

/**
 *
 */
class ExtractExtensionType {

    /** A type variable extractor. */
    private static final TypeVariableExtractor TYPE_LITERAL_EP_EXTRACTOR = TypeVariableExtractor.of(ExtensionMirror.class);

    static Class<? extends Extension<?>> findExtensionType(Class<?> type) {
        Type t = TYPE_LITERAL_EP_EXTRACTOR.extract(type);
//      System.out.println(t);
//      // Check that the subtension have an extension as declaring class
//      ExtensionMember extensionMember = subtensionClass.getAnnotation(ExtensionMember.class);
//      if (extensionMember == null) {
//          throw new InternalExtensionException(subtensionClass + " must be annotated with @ExtensionMember");
//      }

        @SuppressWarnings("unchecked")
        Class<? extends Extension<?>> extensionClass = (Class<? extends Extension<?>>) t;// extensionMember.value();
        // TODO check same module
        // Move to a common method and share it with mirror
        //

        return extensionClass;
    }

//    
//    
//    ClassUtil.checkProperSubclass(ExtensionMirror.class, implementation);
//
//    ExtensionMember em = implementation.getAnnotation(ExtensionMember.class);
//    if (em == null) {
//        throw new InternalExtensionException(implementation + " must be annotated with @ExtensionMember");
//    }
//    Class<? extends Extension<?>> extensionType = em.value();
//    ClassUtil.checkProperSubclass(Extension.class, extensionType); // move
//                                                                   // into
//                                                                   // type
//                                                                   // extractor?
//
//    // Den
//    ClassUtil.checkProperSubclass(Extension.class, extensionType, InternalExtensionException::new); // move into type extractor?
//
//    // Ved ikke om den her er noedvendig??? Vi checker jo om den type extensionen
//    // returnere matcher
//    if (extensionType.getModule() != implementation.getModule()) {
//        throw new InternalExtensionException("The extension mirror " + implementation + " must be a part of the same module ("
//                + extensionType.getModule() + ") as " + extensionType + ", but was part of '" + implementation.getModule() + "'");
//    }

}
