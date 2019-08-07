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
package xests.configsite;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.StackWalker.StackFrame;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import app.packed.app.App;
import app.packed.component.Component;
import app.packed.container.BaseBundle;
import support.util.ConfigSiteTestHelper;

/** Tests {@link App#configSite()}. */
public class AppConfigSiteTest {

    /** Tests that the keep the root configuration site. */
    @Test
    public void configSiteEmptyApp() {
        StackFrame f1 = ConfigSiteTestHelper.caller();
        App app = App.of(new BaseBundle() {
            @Override
            protected void configure() {}
        });

        assertThat(app.configSite()).isNotNull();
        assertThat(app.configSite().hasParent()).isFalse();
        ConfigSiteTestHelper.assertIdenticalPlusLine(f1, 1, app.configSite());
    }

    /** Tests an app with two components. */
    @Test
    public void configSiteTwoComponentsOneContainer() {
        AtomicReference<StackFrame> ar = new AtomicReference<>();

        StackFrame f1 = ConfigSiteTestHelper.caller();
        App app = App.of(new BaseBundle() {
            @Override
            protected void configure() {
                ar.set(ConfigSiteTestHelper.caller());
                install("foo").setName("foo");
                install("doo").setName("doo");
            }
        });

        Component container = app.useComponent("/");
        Component component1 = app.useComponent("foo");
        Component component2 = app.useComponent("doo");

        // Test Container
        assertThat(container.configSite()).isSameAs(app.configSite());
        assertThat(container.configSite().hasParent()).isFalse();
        ConfigSiteTestHelper.assertIdenticalPlusLine(f1, 1, container.configSite());

        // Test Component 1
        assertThat(component1.configSite().hasParent()).isTrue();
        assertThat(component1.configSite().parent().get()).isSameAs(container.configSite());
        ConfigSiteTestHelper.assertIdenticalPlusLine(ar.get(), 1, component1.configSite());

        // Test Component 1
        assertThat(component2.configSite().hasParent()).isTrue();
        assertThat(component2.configSite().parent().get()).isSameAs(container.configSite());
        ConfigSiteTestHelper.assertIdenticalPlusLine(ar.get(), 2, component2.configSite());

    }

    /** Tests an app with a child container. */
    @Test
    public void configSiteComponentInContainerContainer() {
        AtomicReference<StackFrame> ar1 = new AtomicReference<>();
        AtomicReference<StackFrame> ar2 = new AtomicReference<>();

        StackFrame f1 = ConfigSiteTestHelper.caller();
        App app = App.of(new BaseBundle() {
            @Override
            protected void configure() {
                ar1.set(ConfigSiteTestHelper.caller());
                link(new BaseBundle() {
                    @Override
                    public void configure() {
                        setName("woo");
                        ar2.set(ConfigSiteTestHelper.caller());
                        install("foo").setName("foo");
                    }
                });
            }
        });

        Component root = app.useComponent("/");
        Component container = app.useComponent("/woo");
        Component component = app.useComponent("/woo/foo");

        // Test Root
        assertThat(root.configSite()).isSameAs(app.configSite());
        assertThat(root.configSite().hasParent()).isFalse();
        ConfigSiteTestHelper.assertIdenticalPlusLine(f1, 1, root.configSite());

        // Test Container
        assertThat(container.configSite().hasParent()).isTrue();
        assertThat(container.configSite().parent().get()).isSameAs(root.configSite());
        ConfigSiteTestHelper.assertIdenticalPlusLine(ar1.get(), 1, container.configSite());

        // Test Component
        assertThat(component.configSite().hasParent()).isTrue();
        assertThat(component.configSite().parent().get()).isSameAs(container.configSite());
        ConfigSiteTestHelper.assertIdenticalPlusLine(ar2.get(), 1, component.configSite());
    }
}
