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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import app.packed.base.TreePath;

/**
 *
 */
public class PackedComponentPathTest {

    @Test
    public void root() {
        checkPath(PackedTreePath.ROOT, "/");
        checkPath(new PackedTreePath("Foo"), "/Foo");
        checkPath(new PackedTreePath("Foo", "Boo"), "/Foo/Boo");
    }

    private static void checkPath(TreePath p, String expected) {

        ///////// Object
        // hashCode
        assertThat(p.hashCode()).isEqualTo(p.hashCode());
        // TODO create some tests
        // assertThat(p.hashCode()).isEqualTo(expected.hashCode());

        // Equals

        // toString();
        assertThat(p.toString()).isEqualTo(expected);
        assertThat(p.toString()).isSameAs(p.toString());// Make sure we cache the created string

        ///////// CharSequence
        // charAt
        for (int i = 0; i < expected.length(); i++) {
            assertThat(p.charAt(i)).isEqualTo(expected.charAt(i));
        }
        assertThatThrownBy(() -> p.charAt(expected.length())).isExactlyInstanceOf(StringIndexOutOfBoundsException.class);

        // chars()
        assertThat(p.chars()).containsExactlyElementsOf(expected.chars().boxed().collect(Collectors.toList()));

        // codePoints()
        assertThat(p.codePoints()).containsExactlyElementsOf(expected.codePoints().boxed().collect(Collectors.toList()));

        // length
        assertThat(p.length()).isEqualTo(expected.length());

        // TODO subSequence(int, int)

        ///////// Comparable
        // TODO compareTo

        ///////// Component Path
        assertThat(p.isRoot()).isEqualTo(expected.equals("/"));

        if (p.isRoot()) {
            assertThat(p.depth()).isEqualTo(0);
            CharSequence cs = p.parent();
            assertThat(cs).isNull();
        } else {
            int expectedDepth = expected.length() - expected.replace("/", "").length();
            assertThat(p.depth()).isEqualTo(expectedDepth);
            if (p.depth() == 1) {
                checkPath(p.parent(), "/");
            } else {
                String s = expected.substring(0, expected.lastIndexOf('/'));
                checkPath(p.parent(), s);
            }
        }
    }
}
