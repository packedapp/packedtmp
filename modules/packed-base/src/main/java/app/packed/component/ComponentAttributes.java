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
import app.packed.base.TreePath;
import app.packed.base.TypeLiteral;
import app.packed.container.Extension;

/**
 *
 */
// Tilbage paa components??
// Eller man kunne jo bruge et Lookup object for method handle???
public class ComponentAttributes {

    // Save this on TypeLiteral??
    static final TypeLiteral<Class<?>> _CLASS = new TypeLiteral<Class<?>>() {};
    static final TypeLiteral<Class<? extends Extension>> _EXTENSION = new TypeLiteral<Class<? extends Extension>>() {};
    static final TypeLiteral<TypeLiteral<?>> _TYPE_LITERAL = new TypeLiteral<TypeLiteral<?>>() {};

    /** An attribute that accompanies any component that has the {@link ComponentModifier#SOURCED} modifier. */
    public static final Attribute<Class<?>> SOURCE_TYPE = Attribute.of(MethodHandles.lookup(), "source-type", _CLASS);

    /**
     * An attribute that accompanies any component that has the {@link ComponentModifier#EXTENSION} modifier. Subclasses of
     * {@link Extension} are trivially member of itself.
     */
    public static final Attribute<Class<? extends Extension>> EXTENSION_MEMBER = Attribute.of(MethodHandles.lookup(), "extension-member", _EXTENSION);

    /** An attribute that accompanies any component that has the {@link ComponentModifier#SHELL} modifier. */
    public static final Attribute<Class<?>> SHELL_TYPE = Attribute.of(MethodHandles.lookup(), "shell-type", _CLASS);

    ////////////////////// Think about these for a bit //////////////////////////

    // HMM ER DET IKKE BARE SOURCE_TYPE???? Nej, fordi hvis man laver en singleton fra en bundle type har den begge.

    // Er det alle komponenter, ogsaa extensions??? eller kun root komponenten????
    /** An attribute that accompanies any component that has the {@link ComponentModifier#SOURCED} modifier. */
    static final Attribute<Class<? extends Bundle<?>>> BUNDLE_TYPE = Attribute.of(MethodHandles.lookup(), "bundle",
            new TypeLiteral<Class<? extends Bundle<?>>>() {});

    /**
     * 
     * @see ComponentModifier#FUNCTION
     */
    public static final Attribute<TypeLiteral<?>> FUNCTION_TYPE = Attribute.of(MethodHandles.lookup(), "function-type", _TYPE_LITERAL);

    // When something is created from an image, it will have the image path set
    // What Aboun generation?? MAYBE an IMAGE_GENERATION as well?? Or maybe Image names are never reused???
    // A root image will have "/" or /.system.image if restartable...
    // Maybe on GuestImage instead???
    public static final Attribute<TreePath> IMAGE_PATH = Attribute.of(MethodHandles.lookup(), "image-path", TreePath.class);
}
