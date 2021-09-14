package packed.internal.container;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.Test;

import app.packed.extension.Extension;
import app.packed.extension.ExtensionMember;
import app.packed.extension.ExtensionSupport;
import app.packed.extension.InternalExtensionException;
import app.packed.service.ServiceExtension;
import packed.internal.container.SubtensionModelTest.TestExtension.SubStatic;
import packed.internal.container.SubtensionModelTest.TestExtension.TestExtensionSupport;
import testutil.stubs.Throwables;

/** Tests {@link ExtensionSupportModel}. */
public class SubtensionModelTest {

    TestExtension te = new TestExtension();

    /** Tests common functionality. */
    @Test
    public void common() {
        ExtensionSupportModel sm1 = ExtensionSupportModel.of(TestExtension.TestExtensionSupport.class);
        ExtensionSupportModel sm2 = ExtensionSupportModel.of(TestExtension.SubStatic.class);

        assertThat(sm1.extensionType()).isSameAs(TestExtension.class);
        assertThat(sm2.extensionType()).isSameAs(TestExtension.class);

        TestExtensionSupport s = (TestExtensionSupport) sm1.newInstance(te, ServiceExtension.class);
        assertThat(s.getOuter()).isSameAs(te);
        assertThat(s.requestor).isSameAs(ServiceExtension.class);

        assertThat(sm2.newInstance(te, ServiceExtension.class)).isInstanceOf(SubStatic.class);
    }

    /** Tests that the subtension has an {@link Extension} as the declaring class. */
    @Test
    public void invalidDeclaringClass() {
        class NoDeclaringClass extends ExtensionSupport {}
        assertThatThrownBy(() -> ExtensionSupportModel.of(NoDeclaringClass.class)).isExactlyInstanceOf(InternalExtensionException.class);
    }

    /** Tests that we wrap exceptions in {@link InternalExtensionException}. */
    @Test
    public void throwingConstructor() {
        ExtensionSupportModel sm = ExtensionSupportModel.of(TestExtension.SubThrowingConstructor.class);
        AbstractThrowableAssert<?, ? extends Throwable> a = assertThatThrownBy(() -> sm.newInstance(te, ServiceExtension.class));
        a.isExactlyInstanceOf(InternalExtensionException.class);
        a.hasCause(Throwables.RuntimeException1.INSTANCE);
    }

    /** Test that we throw {@link InternalExtensionException} for unresolvable parameters. */
    @Test
    public void unresolvedConstructor() {
        // We do not currently fail on unresolved types???
        
        // SubtensionModel sm = SubtensionModel.of(TestExtension.UnresolvedConstructor.class);
        // System.out.println(sm);
        // sm.newInstance(new TestExtension(), ServiceExtension.class);
        // TODO fix...
        // Should throw an InternalExtensionException if illegal parameters
//        assertThatThrownBy(() -> SubtensionModel.of(UnresolvedConstructor.class)).isExactlyInstanceOf(InternalExtensionException.class);
    }

    static class TestExtension extends Extension {

        @ExtensionMember(TestExtension.class)
        class TestExtensionSupport extends ExtensionSupport {
            final Class<? extends Extension> requestor;

            TestExtensionSupport(Class<? extends Extension> requestor) {
                this.requestor = requestor;
            }

            TestExtension getOuter() {
                return TestExtension.this;
            }
        }

        @ExtensionMember(TestExtension.class)
        static class SubStatic extends ExtensionSupport {}

        @ExtensionMember(TestExtension.class)
        class SubThrowingConstructor extends ExtensionSupport {
            SubThrowingConstructor() {
                throw Throwables.RuntimeException1.INSTANCE;
            }
        }

        @ExtensionMember(TestExtension.class)
        class UnresolvedConstructor extends ExtensionSupport {
            UnresolvedConstructor(String hmm) {}
        }
    }

}
