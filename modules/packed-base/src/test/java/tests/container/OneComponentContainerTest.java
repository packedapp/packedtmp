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
package tests.container;

/**
 *
 */

// Add tests for factory, class, ect.... to this class
// Todo instantiate factories

// inject fields + methods...

// Though decisions on lifecycle....

// Mixins (wait with them... maybe not in V0...)
public class OneComponentContainerTest {

    static final String FOO = "foo";
    //
    // public void fromInstanceX() {
    // test(App.of(c -> c.install(FOO)));
    // }
    //
    // private void test(Container c) {
    //
    // }
    //
    // @Test
    // public void fromInstance() {
    // App con = App.of(c -> c.install(FOO));
    // Component root = con.root();
    //
    // assertThat(con.components().toList()).containsExactly(root);
    //
    // assertThat(con.getComponent("/").get()).isSameAs(root);
    // assertThat(con.getComponent(ComponentPath.ROOT).get()).isSameAs(root);
    //
    // // Verify component
    //
    // assertThat(root.children()).isEmpty();
    // assertThat(root.container()).isSameAs(con);
    // assertThat(root.description()).isNull();
    // // assertThat(c.getInstance()).isSameAs(foo);
    // assertThat(root.name()).isNotNull();
    //
    // // We cast the component path to CharSquence because AssertJ have trouble handling objects that both implement
    // // CharSequence and Comparable.
    // CharSequence cs = root.path();
    // assertThat(cs).isEqualTo(ComponentPath.ROOT);
    // assertThat(cs.toString()).isEqualTo("/");
    //
    // assertThat(root.stream().count()).isEqualTo(1);
    // assertThat(root.tags()).isEmpty();
    // }
    //
    // @Test
    // public void service() {
    // Container con = Container.of(c -> c.install(FOO));
    //
    // assertThat(con.with(String.class)).isSameAs(FOO);
    //
    // con.services().forEach(e -> {
    // System.out.println(e.key());
    // });
    //
    // Component c = con.root();
    // ServiceDescriptor s = con.getService(String.class);
    //
    // assertThat(c.configurationSite()).isSameAs(s.configurationSite());
    // assertThat(c.description()).isSameAs(s.description());
    // // assertThat(s.getInstantiationMode()).isSameAs(InstantiationMode.SINGLETON);
    // assertThat(s.key()).isEqualTo(Key.of(String.class));
    // assertThat(s.tags()).isEmpty();
    // }
    // TODO test lifecycle
}
