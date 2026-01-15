/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package internal.app.packed.util;

import org.jspecify.annotations.Nullable;

/**
 * A component path points to a single component in namespace (tree of named components) expressed as a string
 * of characters in which path parts, separated by the delimiting character "/", represent each unique component.
 * <p>
 * Implementations of this interface are, unless otherwise specified, immutable and safe for use by multiple concurrent
 * threads.
 * <p>
 * Two component paths are equal if their string representation are identical. The hash code of a component path is
 * identical to the hash code of its string representation.
 * <p>
 * This interface will be extended with additional methods in the future.
 */
// Iteralble Path??? Hmm, er det fulde paths eller del paths??? Den er lidt forvirrende
// Altsaa taenker den her kan vi inlined...

// Rename to SimplePath???? and move to app.packed.base...
// BasePath????
// TreePath....

// Element? instead of resource. Only because resources have a specific meaning in Java
// IDK ComponentPath...

// component:FooApp:/
// lifetime:FooApp:dfdf/444
// FooExtension.service:DFDFDF:List<String>
// EntryPointExtension.entrypoint:FooApp:124

// ApplicationPath? App:Container:Bean
// ApplicationElementPath
// Foo:dfsdfdsf/sdfsdf:BeanName

//HelloWorld <- Applicationen HelloWorld
//HelloWorld: <-- root containeren in application HelloWorld
//HelloWorld::MyBean <-- A bean in the root container of HelloWorld
//HelloWorld:foo:MyBean:BeanFactory (An operation?)

// Application:HelloWorld
// Container:HelloWorld:/
// Assembly:HelloWorld:/

// ContainerLifetime:HelloWorld:/
// BeanLifetime:App:Container:Bean
// Bean

// Operation (Operations are lazily calculated deterministic
// Binding App:Container:Bean:Operation:1.1.1 <- param (embedded)

//// Fungere ikke godt fordi de er baseret paa en class (key) og ikke en streng
// Vi kan lazy lave dem ligesom operationer.. Og saa bare Added #1, #2
// Extension
// Context: <-- nah fungere ikke super godt context for XContext
// Namespace?
// Service -> Service:App:Container:Main:@Foo_ffdf

//// Operation names are lazily calculated for a whole bean...
//// I think they are only used for mirrors


// Extension? App:Container:FullClassName <--- Tror ikke vi har denne

// foo <- represents an application
// foo:/ <- resents a a container
// foo:/: <- resents a a bean

// Maybe application path. Den er unik for en application...
// Problemet er man ikke kan addressere app/app/app
// Fordi det er en path til en application...

public interface OldApplicationPath extends Comparable<OldApplicationPath>, /* , Iterable<ComponentPath>, */ CharSequence {

    /** A path representing the root resource of a namespace. */
    OldApplicationPath ROOT = PackedNamespacePath.ROOT;

    OldApplicationPath add(OldApplicationPath other);

    /**
     * Returns the number of elements in this path.
     *
     * @return the number of elements in the path, or {@code 0} if this path represents a root resource
     */
    int depth();

    default String namespaceType() {
        // toString() = namespaceType + ":" + path
        return "component";
    }
    /**
     * Returns whether or not this path represent the root resource in a namespace.
     *
     * @return whether or not this path represent the root resource
     */
    boolean isRoot();

    /**
     * Returns the <em>parent path</em>, or null if this path does not have a parent (is a root).
     *
     * @return a path representing the path's parent
     */
    @Nullable
    OldApplicationPath parent();// Should probably be optional??? Or for performance reasons nullable... hmm

    /**
     * Returns the string representation of this component path.
     * <p>
     * The returned path string uses the {@code '/'} character to separate names in the path.
     *
     * @return the string representation of this component path
     */
    @Override
    String toString();

    /**
     * Converts a path string, or a sequence of strings that when joined form a path string, to a {@code ComponentPath}.
     * This method works similar to {@link Path#of(java.net.URI)}.
     *
     * @param first
     *            the path string or initial part of the component path string
     * @param more
     *            additional strings to be joined to form the component path string
     * @return the resulting {@code ComponentPath}
     * @throws IllegalArgumentException
     *             if the specified path string cannot be converted to a {@code ComponentPath}
     */
    static OldApplicationPath of(String first, String... more) {
        throw new UnsupportedOperationException();
    }

    // TODO hashCode contract

}
