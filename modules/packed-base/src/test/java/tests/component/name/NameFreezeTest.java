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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import app.packed.component.BeanConfiguration;
import app.packed.component.Wirelet;
import app.packed.container.ContainerConfiguration;
import testutil.util.AbstractArtifactTest;
import testutil.util.ContainerConfigurationTester;

/**
 * Tests the various ways the name of a component can be frozen.
 * <p>
 * Certain operations requires us to freeze the name of a component when configuring it. This is done in order to avoid
 * nasty surprises later on. For example, if storing the @link ContainerConfiguration#path() of a component when
 * configuring it. And then later on, we change the name of the container in which it is configured, in such a way that
 * the path of the child changes. We want to avoid this.
 */
@Disabled
public class NameFreezeTest extends AbstractArtifactTest {

    /**
     * Tests that we cannot call {@link BeanConfiguration#setName(String)}. After
     * {@link BeanConfiguration#getName()}.
     */
    @Test
    public void component_setName_cannotBeCalledAfter_getName() {
        checkThrowsISE(c -> {
            BeanConfiguration<?> ci = c.installInstance(1);
            ci.getName();
            ci.setName("foo");
        }, "Cannot call #setName(String) after the name has been initialized via calls to #getName()");
    }

    @Disabled
    @Test
    public void component_setName_cannotBeCalledAfter_install() {
        checkThrowsISE(c -> {
            BeanConfiguration<?> ci = c.installInstance(1);
            c.installInstance(1L);
            ci.setName("foo");
        }, "Cannot call this method after having installed components or used extensions");

        // TODO we should actually have, more or less all the different kind of installs we have in
        // ComponentExtension
    }

    @Disabled
    @Test
    public void component_setName_cannotBeCalledAfter_link() {
        checkThrowsISE(c -> {
            BeanConfiguration<?> ci = c.installInstance(1);
            c.link(emptyBundle());
            ci.setName("foo");
        }, "Cannot call this method after #link() has been invoked");
    }

    @Test
    public void component_setName_cannotBeCalledAfter_path() {
        checkThrowsISE(c -> {
            BeanConfiguration<?> ci = c.installInstance(1);
            ci.path();
            ci.setName("foo");
        }, "Cannot call #setName(String) after name has been initialized via calls to #path()");
    }

    @Test
    public void component_setName_cannotBeCalledAfter_setName() {
        checkThrowsISE(c -> {
            BeanConfiguration<?> ci = c.installInstance(1);
            ci.setName("foo");
            ci.setName("foo");
        }, "#setName(String) can only be called once");
    }

    /**
     * Test that we cannot call {@link ContainerConfiguration#setName(String)} after having observed the name via
     * {@link ContainerConfiguration#getName()}.
     */
    @Test
    public void container_setName_cannotBeCalledAfter_getName() {
        checkThrowsISE(c -> c.getNameIs("Container").setName("Bar"),
                "Cannot call #setName(String) after the name has been initialized via calls to #getName()");
    }

    /**
     * Test that we cannot call {@link ContainerConfiguration#setName(String)} after having installed a component in the
     * container.
     * <p>
     * We could actually allow this as long as any new components does not observe their path in any way. However, for
     * simplicity reasons we just outlaw is always.
     */
    @Test
    @Disabled
    // Problemet er at install bruger ComponentExtension.
    // Og det er maaske lidt skjult naar man bruger f.eks. Bundle...
    // Saa lad os lige gennemtaenke det igen...
    public void container_setName_cannotBeCalledAfter_install() {
        checkThrowsISE(c -> c.installInstance("Foo").setName("Bar"), "Cannot call this method after having installed components");
        // TODO we should actually have, more or less all the different kind of installs we have in
        // ComponentExtension
    }

    /**
     * Test that we cannot call {@link ContainerConfiguration#setName(String)} after having linked another container via
     * {@link ContainerConfiguration#link(app.packed.component.Bundle, Wirelet...)}.
     * <p>
     * We could actually allow this as long as the bundle we link did not observe the path of its components in any way.
     * However, it would be very fragile, if the child component suddenly decided to do it at some point. So better to
     * outlaw is always.
     */
    @Test
    @Disabled
    public void container_setName_cannotBeCalledAfter_link() {
        checkThrowsISE(c -> c.link(emptyBundle()).setName("Bar"), "Cannot call this method after #link() has been invoked");
    }

    /**
     * Test that we cannot call {@link ContainerConfiguration#setName(String)} after having observed the name via
     * {@link ContainerConfiguration#path()}.
     */
    @Test
    public void container_setName_cannotBeCalledAfter_path() {
        checkThrowsISE(c -> c.pathIs("/").setName("Bar"), "Cannot call #setName(String) after name has been initialized via calls to #path()");
    }

    /** Test that we can only call {@link ContainerConfiguration#setName(String)} once. */
    @Test
    public void container_setName_cannotBeCalledAfter_setName() {
        // TODO should we drop this, I actually can't see any problems with this.
        checkThrowsISE(c -> c.setName("Foo").setName("Bar"), "#setName(String) can only be called once");
    }

    private static void checkThrowsISE(Consumer<? super ContainerConfigurationTester> action, String message) {
        assertThatIllegalStateException().isThrownBy(() -> appOf(action)).withNoCause().withMessage(message);

        // TODO test for children as well

    }
}
