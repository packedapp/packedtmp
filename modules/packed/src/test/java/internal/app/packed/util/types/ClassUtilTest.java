package internal.app.packed.util.types;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import org.junit.jupiter.api.Test;

import internal.app.packed.util.types.TypeUtilTest.NestedNonStaticClass;
import internal.app.packed.util.types.TypeUtilTest.NestedStaticClass;

/** Tests {@link ClassUtil}. */
public class ClassUtilTest {

//    /** Tests {@link ClassUtil#checkIsInstantiable(Class)} */
//    @Test
//    public void checkIsInstantiable() {
//        try {
//            ClassUtil.checkIsInstantiable(Test.class);
//            fail("oops");
//        } catch (IllegalArgumentException ok) {}
//        try {
//            ClassUtil.checkIsInstantiable(Map.class);
//            fail("oops");
//        } catch (IllegalArgumentException ok) {}
//        try {
//            ClassUtil.checkIsInstantiable(Object[].class);
//            fail("oops");
//        } catch (IllegalArgumentException ok) {}
//        try {
//            ClassUtil.checkIsInstantiable(AbstractMap.class);
//            fail("oops");
//        } catch (IllegalArgumentException ok) {}
//
//        try {
//            ClassUtil.checkIsInstantiable(Integer.TYPE);
//            fail("oops");
//        } catch (IllegalArgumentException ok) {}
//
//        assertThat(ClassUtil.checkIsInstantiable(HashMap.class)).isSameAs(HashMap.class);
//    }

    /** Tests {@link ClassUtil#isInnerOrLocal(Class)}. */
    @Test
    public void isInnerOrLocal() {
        assertThat(ClassUtil.isInnerOrLocal(ClassUtilTest.class)).isFalse();
        assertThat(ClassUtil.isInnerOrLocal(NestedStaticClass.class)).isFalse();

        class LocalClass {}
        assertThat(ClassUtil.isInnerOrLocal(LocalClass.class)).isTrue();
        assertThat(ClassUtil.isInnerOrLocal(NestedNonStaticClass.class)).isTrue();
        // TODO should we include anonymous class??
        // assertThat(ClassUtil.isInnerOrLocalClass(new Object() {}.getClass())).isTrue();
    }


    /** Tests {@link ClassUtil#isOptionalType(Class)}. */
    @Test
    public void isOptional() {
        assertThat(ClassUtil.isOptionalType(String.class)).isFalse();
        assertThat(ClassUtil.isOptionalType(null)).isFalse();
        assertThat(ClassUtil.isOptionalType(Optional.class)).isTrue();
        assertThat(ClassUtil.isOptionalType(OptionalLong.class)).isTrue();
        assertThat(ClassUtil.isOptionalType(OptionalInt.class)).isTrue();
        assertThat(ClassUtil.isOptionalType(OptionalDouble.class)).isTrue();
    }

    /** Tests {@link ClassUtil#unbox(Class)}. */
    @Test
    public void unbox() {
        assertThat(ClassUtil.unbox(String.class)).isSameAs(String.class);
        assertThat(ClassUtil.unbox(Boolean.class)).isSameAs(boolean.class);
        assertThat(ClassUtil.unbox(Byte.class)).isSameAs(byte.class);
        assertThat(ClassUtil.unbox(Character.class)).isSameAs(char.class);
        assertThat(ClassUtil.unbox(Double.class)).isSameAs(double.class);
        assertThat(ClassUtil.unbox(Float.class)).isSameAs(float.class);
        assertThat(ClassUtil.unbox(Integer.class)).isSameAs(int.class);
        assertThat(ClassUtil.unbox(Long.class)).isSameAs(long.class);
        assertThat(ClassUtil.unbox(Short.class)).isSameAs(short.class);
        assertThat(ClassUtil.unbox(Void.class)).isSameAs(void.class);
    }

    /** Tests {@link ClassUtil#box(Class)}. */
    @Test
    public void box() {
        assertThat(ClassUtil.box(String.class)).isSameAs(String.class);
        assertThat(ClassUtil.box(boolean.class)).isSameAs(Boolean.class);
        assertThat(ClassUtil.box(byte.class)).isSameAs(Byte.class);
        assertThat(ClassUtil.box(char.class)).isSameAs(Character.class);
        assertThat(ClassUtil.box(double.class)).isSameAs(Double.class);
        assertThat(ClassUtil.box(float.class)).isSameAs(Float.class);
        assertThat(ClassUtil.box(int.class)).isSameAs(Integer.class);
        assertThat(ClassUtil.box(long.class)).isSameAs(Long.class);
        assertThat(ClassUtil.box(short.class)).isSameAs(Short.class);
        assertThat(ClassUtil.box(void.class)).isSameAs(Void.class);
    }
}
