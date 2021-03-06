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
package packed.internal.component;

import java.lang.invoke.MethodHandles;

import app.packed.attribute.Attribute;
import app.packed.base.NamespacePath;
import app.packed.base.TypeToken;

/**
 *
 */
// Tilbage paa components?? Syntes maaske det er fint at X (extends AttributeHolder) har en XAttributes klasse
// Hvis den altsaa definere custom annoteringer

// Eller man kunne jo bruge et Lookup object for method handle???

// Must be defined in a static field

final class ComponentAttributes {

    // Save this on TypeLiteral??
    static final TypeToken<Class<?>> _CLASS = new TypeToken<Class<?>>() {};
    
    // Kommer vi til at bruge den andre steder???
    //static final TypeToken<Class<? extends Extension>> _EXTENSION_TYPE = new TypeToken<Class<? extends Extension>>() {};
    static final TypeToken<TypeToken<?>> _TYPE_LITERAL = new TypeToken<TypeToken<?>>() {};

//    /** An attribute that accompanies any component that has the {@link ComponentModifier#APPLICATION} modifier. */
//    public static final Attribute<Class<?>> APPLICATION_CLASS = Attribute.of(MethodHandles.lookup(), "application-class", _CLASS);
//
//    // Er det alle komponenter, ogsaa extensions??? eller kun root komponenten????
//    /** An attribute that accompanies any component that has the {@link ComponentModifier#SOURCED} modifier. */
//    static final Attribute<Class<? extends Assembly<?>>> CONTAINER_TYPE = Attribute.of(MethodHandles.lookup(), "container",
//            new TypeToken<Class<? extends Assembly<?>>>() {});

    /** An attribute that accompanies any component that is part of an an extension. */
    // Altsaa maaske skal vi have to typer??? maaske hedder den bare E
    // app.packed/extension-type
    // extension-type@app.packed
    //public static final Attribute<Class<? extends Extension>> EXTENSION_TYPE = Attribute.of(MethodHandles.lookup(), "extension-type", new TypeToken<Class<? extends Extension>>() {});

    ////////////////////// Think about these for a bit //////////////////////////

    // HMM ER DET IKKE BARE SOURCE_TYPE???? Nej, fordi hvis man laver en singleton fra en assembly type har den begge.

    //// Component Instance taenker jeg...dd
    // When something is created from an image, it will have the image path set
    // What Aboun generation?? MAYBE an IMAGE_GENERATION as well?? Or maybe Image names are never reused???
    // A root image will have "/" or /.system.image if restartable...
    // Maybe on GuestImage instead???
    public static final Attribute<NamespacePath> IMAGE_PATH = Attribute.of(MethodHandles.lookup(), "image-path", NamespacePath.class);

//    /** An attribute that is available on any component with the {@link ComponentModifier#BUILD} modifier. */
//    // Ved ikke praecis
//    public static final Attribute<BuildModel> BUILD_INFO = Attribute.of(MethodHandles.lookup(), "build-info", BuildModel.class);

}
