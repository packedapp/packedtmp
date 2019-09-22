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
package tests.component.name;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentExtension;
import app.packed.container.ContainerConfiguration;
import support.testutil.AbstractArtifactTest;
import support.testutil.ContainerConfigurationTester;

/**
 * Tests the various ways the name of a component can be frozen.
 * <p>
 * Certain operations requires us to freeze the name of a component when configuring it. This is done in order to avoid
 * nasty surprises later on. For example, if storing the @link ContainerConfiguration#path() of a component when
 * configuring it. And then later on, we change the name of the container in which it is configured, in such a way that
 * the path of the child changes. We want to avoid this.
 */
public class NameFreezeTest extends AbstractArtifactTest {

    @Test
    public void component_setName_cannotBeCalledAfter_getName() {
        checkThrowsISE(c -> {
            c.use(ComponentExtension.class, e -> {
                ComponentConfiguration ci = e.installInstance(1);
                ci.getName();
                ci.setName("foo");
            });
        }, "Cannot call #setName(String) after name has been initialized via call to #getName()");
    }

    @Test
    public void component_setName_cannotBeCalledAfter_install() {
        checkThrowsISE(c -> {
            c.use(ComponentExtension.class, e -> {
                ComponentConfiguration ci = e.installInstance(1);
                e.installInstance(1L);
                ci.setName("foo");
            });
        }, "Cannot call this method after installing new components in the container");

        // TODO we should actually have, more or less all the different kind of installs we have in
        // ComponentExtension
    }

    @Test
    public void component_setName_cannotBeCalledAfter_link() {
        checkThrowsISE(c -> {
            c.use(ComponentExtension.class, e -> {
                ComponentConfiguration ci = e.installInstance(1);
                c.link(EMPTY_BUNDLE);
                ci.setName("foo");
            });
        }, "Cannot call this method after containerConfiguration.link has been invoked");
    }

    @Test
    public void component_setName_cannotBeCalledAfter_path() {
        checkThrowsISE(c -> {
            c.use(ComponentExtension.class, e -> {
                ComponentConfiguration ci = e.installInstance(1);
                ci.path();
                ci.setName("foo");
            });
        }, "Cannot call #setName(String) after name has been initialized via call to #path()");
    }

    @Test
    public void component_setName_cannotBeCalledAfter_setName() {
        checkThrowsISE(c -> {
            c.use(ComponentExtension.class, e -> {
                ComponentConfiguration ci = e.installInstance(1);
                ci.setName("foo");
                ci.setName("foo");
            });
        }, "#setName(String) can only be called once");
    }

    /**
     * Test that we cannot call {@link ContainerConfiguration#setName(String)} after having observed the name via
     * {@link ContainerConfiguration#getName()}.
     */
    @Test
    public void container_setName_cannotBeCalledAfter_getName() {
        checkThrowsISE(c -> c.getNameIs("Container").setName("Bar"), "Cannot call #setName(String) after name has been initialized via call to #getName()");
    }

    /**
     * Test that we cannot call {@link ContainerConfiguration#setName(String)} after having installed a component in the
     * container.
     * <p>
     * We could actually allow this as long as any new components does not observe their path in any way. However, for
     * simplicity reasons we just outlaw is always.
     */
    @Test
    public void container_setName_cannotBeCalledAfter_install() {
        checkThrowsISE(c -> c.use(ComponentExtension.class, e -> e.installInstance("Foo")).setName("Bar"),
                "Cannot call this method after installing new components in the container");
        // TODO we should actually have, more or less all the different kind of installs we have in
        // ComponentExtension
    }

    /**
     * Test that we cannot call {@link ContainerConfiguration#setName(String)} after having linked another container via
     * {@link ContainerConfiguration#link(app.packed.container.Bundle, app.packed.container.Wirelet...)}.
     * <p>
     * We could actually allow this as long as the bundle we link did not observe the path of its components in any way.
     * However, it would be very fragile, if the child component suddenly decided to do it at some point. So better to
     * outlaw is always.
     */
    @Test
    public void container_setName_cannotBeCalledAfter_link() {
        checkThrowsISE(c -> c.link(EMPTY_BUNDLE).setName("Bar"), "Cannot call this method after containerConfiguration.link has been invoked");
    }

    /**
     * Test that we cannot call {@link ContainerConfiguration#setName(String)} after having observed the name via
     * {@link ContainerConfiguration#path()}.
     */
    @Test
    public void container_setName_cannotBeCalledAfter_path() {
        checkThrowsISE(c -> c.pathIs("/").setName("Bar"), "Cannot call #setName(String) after name has been initialized via call to #path()");
    }

    /** Test that we can only call {@link ContainerConfiguration#setName(String)} once. */
    @Test
    public void container_setName_cannotBeCalledAfter_setName() {
        // TODO should we drop this, I actually can't see any problems with this.
        checkThrowsISE(c -> c.setName("Foo").setName("Bar"), "#setName(String) can only be called once");
    }

    private static void checkThrowsISE(Consumer<? super ContainerConfigurationTester> source, String message) {
        assertThatIllegalStateException().isThrownBy(() -> appOf(source)).withNoCause().withMessage(message);

        // TODO test for children as well

    }
}
