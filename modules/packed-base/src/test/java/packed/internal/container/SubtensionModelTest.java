package packed.internal.container;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.Test;

import app.packed.container.Extension;
import app.packed.container.Extension.Subtension;
import app.packed.container.InternalExtensionException;
import app.packed.inject.ServiceExtension;
import packed.internal.container.SubtensionModelTest.TestExtension.Sub;
import packed.internal.container.SubtensionModelTest.TestExtension.SubStatic;
import testutil.stubs.Throwables;

/** Tests {@link SubtensionModel}. */
public class SubtensionModelTest {

    TestExtension te = new TestExtension();

    /** Tests common functionality. */
    @Test
    public void common() {
        SubtensionModel sm1 = SubtensionModel.of(TestExtension.Sub.class);
        SubtensionModel sm2 = SubtensionModel.of(TestExtension.SubStatic.class);

        assertThat(sm1.extensionClass).isSameAs(TestExtension.class);
        assertThat(sm2.extensionClass).isSameAs(TestExtension.class);

        
        Sub s = (Sub) sm1.newInstance(te, ServiceExtension.class);
        assertThat(s.getOuter()).isSameAs(te);
        assertThat(s.requestor).isSameAs(ServiceExtension.class);

        assertThat(sm2.newInstance(te, ServiceExtension.class)).isInstanceOf(SubStatic.class);
    }

    /** Tests that the subtension has an extension as a declaring class. */
    @Test
    public void invalidDeclaringClass() {
        class NoDeclaringClass extends Subtension {}
        assertThatThrownBy(() -> SubtensionModel.of(NoDeclaringClass.class)).isExactlyInstanceOf(InternalExtensionException.class);
    }

    /** Tests that we wrap exceptions in {@link InternalExtensionException}. */
    @Test
    public void throwingConstructor() {
        SubtensionModel sm = SubtensionModel.of(TestExtension.SubThrowingConstructor.class);
        AbstractThrowableAssert<?, ? extends Throwable> a = assertThatThrownBy(() -> sm.newInstance(te, ServiceExtension.class));
        a.isExactlyInstanceOf(InternalExtensionException.class);
        a.hasCause(Throwables.RuntimeException1.INSTANCE);
    }

    /** Test that we throw {@link InternalExtensionException} for unresolvable parameters. */
    @Test
    public void unresolvedConstructor() {
        // TODO fix...
        // Should throw an InternalExtensionException if illegal parameters
//        assertThatThrownBy(() -> SubtensionModel.of(UnresolvedConstructor.class)).isExactlyInstanceOf(InternalExtensionException.class);
    }

    static class TestExtension extends Extension {

        class Sub extends Subtension {
            final Class<? extends Extension> requestor;

            Sub(Class<? extends Extension> requestor) {
                this.requestor = requestor;
            }

            TestExtension getOuter() {
                return TestExtension.this;
            }
        }

        static class SubStatic extends Subtension {}

        class SubThrowingConstructor extends Subtension {
            SubThrowingConstructor() {
                throw Throwables.RuntimeException1.INSTANCE;
            }
        }

        class UnresolvedConstructor extends Subtension {
            UnresolvedConstructor(String hmm) {}
        }
    }
}
