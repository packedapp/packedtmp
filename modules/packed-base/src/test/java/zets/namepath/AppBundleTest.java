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
package zets.namepath;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import app.packed.app.App;
import app.packed.container.Bundle;
import app.packed.container.Wirelet;

/**
 *
 */
public class AppBundleTest {

    @Test
    public void appNames() {
        class Bundle extends ActionBundle {}
        appNames0(() -> new Bundle(), "App");

        class HelloWorld extends ActionBundle {}
        appNames0(() -> new HelloWorld(), "HelloWorld");

        class HelloWorldBundle extends ActionBundle {}
        appNames0(() -> new HelloWorldBundle(), "HelloWorld");
    }

    private void appNames0(Supplier<ActionBundle> cs, String defaultName) {
        // Tests no actions
        assertThat(App.of(cs.get().none()).name()).isEqualTo(defaultName);
        assertThat(App.of(cs.get().none(), Wirelet.name("Boo")).name()).isEqualTo("Boo");
        assertThat(App.of(cs.get().none(), Wirelet.name("Boo"), Wirelet.name("Goo")).name()).isEqualTo("Goo");

        // Tests that getName() forces the calculation of the name
        assertThat(App.of(cs.get().get(defaultName)).name()).isEqualTo(defaultName);
        assertThat(App.of(cs.get().get("Boo"), Wirelet.name("Boo")).name()).isEqualTo("Boo");
        assertThat(App.of(cs.get().get("Goo"), Wirelet.name("Boo"), Wirelet.name("Goo")).name()).isEqualTo("Goo");

        // Tests setAction
        assertThat(App.of(cs.get().set("Foo", "Foo")).name()).isEqualTo("Foo");
        assertThat(App.of(cs.get().set("Foo", "Boo"), Wirelet.name("Boo")).name()).isEqualTo("Boo");
        assertThat(App.of(cs.get().set("Foo", "Goo"), Wirelet.name("Boo"), Wirelet.name("Goo")).name()).isEqualTo("Goo");
    }

    private abstract static class ActionBundle extends Bundle {
        private Action action;
        private String expectedName;
        private String setName;

        ActionBundle none() {
            this.action = Action.NONE;
            return this;
        }

        ActionBundle get(String expectedName) {
            this.action = Action.GET_NAME;
            this.expectedName = requireNonNull(expectedName);
            return this;
        }

        ActionBundle set(String setName, String expectedName) {
            this.action = Action.SET_NAME;
            this.setName = setName;
            this.expectedName = expectedName;
            return this;
        }

        @Override
        public void configure() {
            switch (action) {
            case GET_NAME:
                assertThat(getName()).isEqualTo(expectedName);
                break;
            case SET_NAME:
                setName(setName);
                assertThat(getName()).isEqualTo(expectedName);
            default:
                // DO Nothing
            }
        }
    }

    private enum Action {
        NONE, SET_NAME, GET_NAME;
    }
}
