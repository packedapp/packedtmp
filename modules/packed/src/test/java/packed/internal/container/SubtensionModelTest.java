package packed.internal.container;

import org.junit.jupiter.api.Test;

import app.packed.container.Extension;
import app.packed.container.ExtensionPoint;
import app.packed.container.InternalExtensionException;
import testutil.stubs.Throwables;

public class SubtensionModelTest {

    TestExtension te = new TestExtension();

    // Disse test er udkommenterede efter vi introducere ExtensionSupportContext
    // Nu tager vi et ExtensionTree... og det er bare knapt saa let at simulere
    
//    /** Tests common functionality. */
//    @Test
//    public void common() {
//        ExtensionSupportModel sm1 = ExtensionSupportModel.of(TestExtension.TestExtensionSupport.class);
//        ExtensionSupportModel sm2 = ExtensionSupportModel.of(TestExtension.SubStatic.class);
//
//        assertThat(sm1.extensionType()).isSameAs(TestExtension.class);
//        assertThat(sm2.extensionType()).isSameAs(TestExtension.class);
//
//        TestExtensionSupport s = (TestExtensionSupport) sm1.newInstance(te, ServiceExtension.class);
//        assertThat(s.getOuter()).isSameAs(te);
//        assertThat(s.requestor).isSameAs(ServiceExtension.class);
//
//        assertThat(sm2.newInstance(te, ServiceExtension.class)).isInstanceOf(SubStatic.class);
//    }

    /** Tests that the subtension has an {@link Extension} as the declaring class. */
//    @Test
//    public void invalidDeclaringClass() {
//        class NoDeclaringClass extends ExtensionPoint {}
//        assertThatThrownBy(() -> ExtensionSupportModel.of(NoDeclaringClass.class)).isExactlyInstanceOf(InternalExtensionException.class);
//    }

//    /** Tests that we wrap exceptions in {@link InternalExtensionException}. */
//    @Test
//    public void throwingConstructor() {
//        ExtensionSupportModel sm = ExtensionSupportModel.of(TestExtension.SubThrowingConstructor.class);
//        AbstractThrowableAssert<?, ? extends Throwable> a = assertThatThrownBy(() -> sm.newInstance(te, ServiceExtension.class));
//        a.isExactlyInstanceOf(InternalExtensionException.class);
//        a.hasCause(Throwables.RuntimeException1.INSTANCE);
//    }

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

    static class TestExtension extends Extension<TestExtension> {

        class TestExtensionPoint extends ExtensionPoint<TestExtension> {
            final Class<? extends Extension<?>> requestor;

            TestExtensionPoint(Class<? extends Extension<?>> requestor) {
                this.requestor = requestor;
            }

            TestExtension getOuter() {
                return TestExtension.this;
            }
        }

        static class SubStatic extends ExtensionPoint<TestExtension> {}

        class SubThrowingConstructor extends ExtensionPoint<TestExtension> {
            SubThrowingConstructor() {
                throw Throwables.RuntimeException1.INSTANCE;
            }
        }

        class UnresolvedConstructor extends ExtensionPoint<TestExtension> {
            UnresolvedConstructor(String hmm) {}
        }
    }

}
