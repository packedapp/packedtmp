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
package app.packed.component;

import java.lang.invoke.MethodHandles;

import app.packed.base.Attribute;
import app.packed.base.TypeLiteral;
import app.packed.container.Extension;

/**
 *
 */
// Tilbage paa components??
// Eller man kunne jo bruge et Lookup object for method handle???
public class ComponentAttributes {

    // Save this on TypeLiteral??
    static final TypeLiteral<TypeLiteral<?>> TL_ANY = new TypeLiteral<TypeLiteral<?>>() {};
    static final TypeLiteral<Class<?>> TL_C = new TypeLiteral<Class<?>>() {};
    static final TypeLiteral<Class<? extends Extension>> TL_E = new TypeLiteral<Class<? extends Extension>>() {};

    // HMM ER DET IKKE BARE SOURCE_TYPE???? Nej, fordi hvis man laver en singleton fra en bundle type har den begge.
    static final Attribute<Class<? extends Bundle<?>>> BUNDLE_TYPE = Attribute.of(MethodHandles.lookup(), "bundle",
            new TypeLiteral<Class<? extends Bundle<?>>>() {});

    /**
     * 
     * @see ComponentModifier#FUNCTION
     */
    public static final Attribute<TypeLiteral<?>> FUNCTION_TYPE = Attribute.of(MethodHandles.lookup(), "function-type", TL_ANY);

    public static final Attribute<Class<?>> SOURCE_TYPE = Attribute.of(MethodHandles.lookup(), "source-type", TL_C);

    // An extension is trivial member of itself... (TODO test, that you cannot use ExtensionMember on an extension...)
    public static final Attribute<Class<? extends Extension>> EXTENSION_MEMBER = Attribute.of(MethodHandles.lookup(), "extension-member", TL_E);

    // When something is created from an image, it will have the image path set
    // What Aboun generation?? MAYBE an IMAGE_GENERATION as well?? Or maybe Image names are never reused???
    // A root image will have "/" or /.system.image if restartable...
    // Maybe on GuestImage instead???
    public static final Attribute<ComponentPath> IMAGE_PATH = Attribute.of(MethodHandles.lookup(), "image-path", ComponentPath.class);
}