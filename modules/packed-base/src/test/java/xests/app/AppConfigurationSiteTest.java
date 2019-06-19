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
package xests.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.StackWalker.StackFrame;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import app.packed.app.App;
import app.packed.component.Component;
import app.packed.container.Bundle;
import app.packed.container.Wirelet;

/**
 * Tests {@link App#configurationSite()}.
 */
public class AppConfigurationSiteTest {

    /** Tests that the various bind operations gets the right configuration site. */
    @Test
    public void configSiteEmptyApp() {
        StackFrame f1 = ConfigSiteTestHelper.caller();
        App app = App.of(new Bundle() {
            @Override
            protected void configure() {}
        });

        assertThat(app.configurationSite()).isNotNull();
        assertThat(app.configurationSite().hasParent()).isFalse();
        ConfigSiteTestHelper.assertIdenticalPlusLine(f1, 1, app.configurationSite());
    }

    /** Tests that the various bind operations gets the right configuration site. */
    @Test
    public void configSiteSingleComponent() {
        AtomicReference<StackFrame> ar = new AtomicReference<>();

        StackFrame f1 = ConfigSiteTestHelper.caller();
        App app = App.of(new Bundle() {
            @Override
            protected void configure() {
                ar.set(ConfigSiteTestHelper.caller());
                install("foo").setName("foo");
            }
        });

        Component container = app.useComponent("/");
        Component component = app.useComponent("foo");

        // Test Container
        assertThat(container.configurationSite()).isSameAs(app.configurationSite());
        assertThat(container.configurationSite().hasParent()).isFalse();
        ConfigSiteTestHelper.assertIdenticalPlusLine(f1, 1, container.configurationSite());

        // Test Component
        assertThat(component.configurationSite().hasParent()).isTrue();
        assertThat(component.configurationSite().parent().get()).isSameAs(container.configurationSite());
        ConfigSiteTestHelper.assertIdenticalPlusLine(ar.get(), 1, component.configurationSite());
    }

    /** Tests that the various bind operations gets the right configuration site. */
    @Test
    public void configSiteComponentInContainerContainer() {
        AtomicReference<StackFrame> ar1 = new AtomicReference<>();
        AtomicReference<StackFrame> ar2 = new AtomicReference<>();

        StackFrame f1 = ConfigSiteTestHelper.caller();
        App app = App.of(new Bundle() {
            @Override
            protected void configure() {
                ar1.set(ConfigSiteTestHelper.caller());
                link(new Bundle() {
                    @Override
                    public void configure() {
                        ar2.set(ConfigSiteTestHelper.caller());
                        install("foo").setName("foo");
                    }
                }, Wirelet.name("woo"));
            }
        });

        Component root = app.useComponent("/");
        Component container = app.useComponent("woo");
        Component component = app.useComponent("woo/foo");

        // Test Root
        assertThat(root.configurationSite()).isSameAs(app.configurationSite());
        assertThat(root.configurationSite().hasParent()).isFalse();
        ConfigSiteTestHelper.assertIdenticalPlusLine(f1, 1, root.configurationSite());

        // Test Container
        assertThat(container.configurationSite().hasParent()).isTrue();
        assertThat(container.configurationSite().parent().get()).isSameAs(root.configurationSite());
        ConfigSiteTestHelper.assertIdenticalPlusLine(ar1.get(), 1, container.configurationSite());

        // Test Component
        assertThat(component.configurationSite().hasParent()).isTrue();
        assertThat(component.configurationSite().parent().get()).isSameAs(container.configurationSite());
        ConfigSiteTestHelper.assertIdenticalPlusLine(ar2.get(), 1, component.configurationSite());
    }
}
