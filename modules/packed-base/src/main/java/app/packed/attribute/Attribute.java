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
package app.packed.attribute;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.base.TypeToken;
import packed.internal.base.attribute.PackedAttribute;
import packed.internal.base.attribute.PackedAttribute.PackedOption;

/**
 * <p>
 * Attributes can be compared with value semantics (==)
 * 
 * <p>
 * NOTE: Attributes does not support null values.
 * 
 * @param <T>
 *            the type of value the attribute map to
 */
// What are build time attributes and what runtime attributes???

public /* sealed */ interface Attribute<T> {

    // Er ikke sikker paa jeg vil expose denne??
    default Optional<T> defaultValue() {
        throw new UnsupportedOperationException();
    }

    /**
     * Display the name of this attribute as.
     * 
     * @return display the name of this attribute as
     */
    // Service#name
    String displayAs();

    /**
     * Returns whether or not the attribute is hidden. Attributes whose name start with <code>.</code> are automatically
     * hidden.
     * <p>
     * Hidden attributes will never show in {@link AttributeMap#keys()} or {@link AttributeMap#entrySet()},
     * {@link AttributeMap#isEmpty()}. All other methods work as normal.
     * 
     * @return whether or not the attribute is hidden.
     */
    default boolean isHidden() {
        return name().startsWith(".");
    }

    /**
     * Returns {@code true} if this is an open attribute.
     * <p>
     *
     * An open attribute can always be set by any one.
     *
     * @return {@code true} if this is an open attribute
     */
    boolean isOpen();

    /**
     * Returns the module the attribute belongs to.
     * <p>
     * The module is always the module in which {@link #owner()} is located.
     * 
     * @return the module the attribute belongs to
     */
    default Module module() {
        return owner().getModule();
    }

    /**
     * Returns the name of the attribute. A class should never declared multiple attributes with the same name.
     * 
     * @return the name of the attribute
     */
    String name();

    /**
     * Returns the class that declares the attribute.
     * <p>
     * This class is always identical to the class returned by {@link Lookup#lookupClass()} of the lookup object that is
     * used for construction.
     * 
     * @return the class that declares the attribute
     * 
     * @see Lookup#lookupClass()
     */
    Class<?> owner();

    /**
     * Returns the raw type of this attribute.
     * 
     * @return the raw type of this attribute
     */
    Class<?> rawType();

    /**
     * Returns the type literal of this attribute.
     * 
     * @return the type literal of this attribute
     */
    TypeToken<T> typeLiteral();

    /**
     * Creates a new attribute.
     * 
     * @param <T>
     *            the type of attribute
     * @param lookup
     *            a method handle for the class that defines the attribute
     * @param name
     *            the name of the attribute
     * @param type
     *            the type of the attribute
     * @param options
     *            any options used for constructing the attribute
     * @return the new attribute
     */
    // Also, maybe we have special attribute makers... fx ClassHookBootstrap.newAttribute
    @SafeVarargs
    static <T> Attribute<T> of(Lookup lookup, String name, Class<T> type, Option<T>... options) {
        requireNonNull(type, "type is null");
        return of(lookup, name, TypeToken.of(type), options);
    }

    @SafeVarargs
    static <T> Attribute<T> of(Lookup lookup, String name, TypeToken<T> type, Option<T>... options) {
        requireNonNull(type, "type is null");
        return PackedAttribute.of(lookup, name, type.rawType(), type, options);
    }

    @SafeVarargs
    static <T> Attribute<T> of2(String name, Class<T> type, Option<T>... options) {
        // I think we should use the calling class as owner...
        // And have one where we specify the owner but the owner must be in the same module as the calling class
        // use calling class
        throw new UnsupportedOperationException();
    }

    @SafeVarargs
    static <T> Attribute<T> of2(Class<?> owner, String name, Class<T> type, Option<T>... options) {
        // owner must be in same module as calling class
        // use calling class
        
        // Hooks should maybe take a MethodHandle that can extract the attribute value from
        // the slot table
        throw new UnsupportedOperationException();
    }

    
    // Use a builder...
    // And have methods with an without a builder
    /** Various options that can be specified when creating new attributes. */
    static interface Option<T> {
        // Supportere ikke Nullable. Saa mangler attributen bare..
        // Tror ikke vi behoever denne... vi soerger for de steder vi har brug det at der ikke kan registreres ting for sent...

        //// Hmm IDK.. Optional<T> Attribute.defaultValue();
        // Syntes aergelig talt ikke der skal vaere default vaedier...
        // Det bliver super rodet, fordi vi hver gang er saadan lidt.
        // Skal vi bare returne default vaerdien. Eller skal vi fejle
        // Eller skal vi xyz
        static <T> Option<T> defaultValue(T value) {
            throw new UnsupportedOperationException();
        }

        // Adds a description of the attribute...
        // What about I18N
        static <T> Option<T> description(String description) {
            throw new UnsupportedOperationException();
        }

        static <T> Option<T> mutable() {
            throw new UnsupportedOperationException();
        }
        // Syntes virkelig ikke vi skal supportere Nullable.
        // Saa maa folk lade vaere at tilfoeje attribuetd
        // Nullable();

        // open() openModule();
        // openModule() open to all in same module...
        @SuppressWarnings({ "rawtypes", "unchecked" })
        static <T> Option<T> open() {
            return (Option) PackedOption.someSome();
        }

        // if (injectionGraph.size>2 ? : 333);
        @SuppressWarnings({ "rawtypes", "unchecked" })
        static <T> Option<T> renderAs(Function<T, String> toString) {
            return (Option) PackedOption.someSome();
        }

        static <T> Option<T> toDoc(Consumer<Object> docBuilder) {
            // Kan smide den med i doc...
            throw new UnsupportedOperationException();
        }

//        static Option permanent() {
//            throw new UnsupportedOperationException();
//        }
    }
}
//
///**
// * An attribute can be hidden. Meaning it will not show up in {@link AttributeSet#attributes()} or
// * {@link AttributeSet#entrySet()}. Otherwise it will function as any other attribute. For example, it can still be
// * provided via {@link AttributeProvide}.
// * <p>
// * Can still not provide values unless it is open...
// * 
// * @return a hidden option
// */
//// .XXX -> hidden attribute
//static <T> Option<T> hidden() {
//    // Should we require that hidden options always starts with .
//    // maybe attributes that starts with . are always hidden....
//    // SO maybe we do not need this option...
//    throw new UnsupportedOperationException();
//}
// shortname
// DependsOn

// name
// ServiceExtension#DependsOn

// fullname
// app.packed.service.ServiceExtension#DependsOn

// name = DependsOn, fullName=ServiceExtension#DependsOn

// Bliver der noget saa maa folk skrive det fuldt ud selv.
// Vi kan heller ikke checke ting der er loaded med forskelling class loaders.
