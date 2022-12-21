package internal.app.packed.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import org.junit.jupiter.api.Test;

import internal.app.packed.util.TypeUtilTest.NestedNonStaticClass;
import internal.app.packed.util.TypeUtilTest.NestedStaticClass;

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

    /** Tests {@link ClassUtil#unwrap(Class)}. */
    @Test
    public void unwrap() {
        assertThat(ClassUtil.unwrap(String.class)).isSameAs(String.class);
        assertThat(ClassUtil.unwrap(Boolean.class)).isSameAs(boolean.class);
        assertThat(ClassUtil.unwrap(Byte.class)).isSameAs(byte.class);
        assertThat(ClassUtil.unwrap(Character.class)).isSameAs(char.class);
        assertThat(ClassUtil.unwrap(Double.class)).isSameAs(double.class);
        assertThat(ClassUtil.unwrap(Float.class)).isSameAs(float.class);
        assertThat(ClassUtil.unwrap(Integer.class)).isSameAs(int.class);
        assertThat(ClassUtil.unwrap(Long.class)).isSameAs(long.class);
        assertThat(ClassUtil.unwrap(Short.class)).isSameAs(short.class);
        assertThat(ClassUtil.unwrap(Void.class)).isSameAs(void.class);
    }

    /** Tests {@link ClassUtil#wrap(Class)}. */
    @Test
    public void wrap() {
        assertThat(ClassUtil.wrap(String.class)).isSameAs(String.class);
        assertThat(ClassUtil.wrap(boolean.class)).isSameAs(Boolean.class);
        assertThat(ClassUtil.wrap(byte.class)).isSameAs(Byte.class);
        assertThat(ClassUtil.wrap(char.class)).isSameAs(Character.class);
        assertThat(ClassUtil.wrap(double.class)).isSameAs(Double.class);
        assertThat(ClassUtil.wrap(float.class)).isSameAs(Float.class);
        assertThat(ClassUtil.wrap(int.class)).isSameAs(Integer.class);
        assertThat(ClassUtil.wrap(long.class)).isSameAs(Long.class);
        assertThat(ClassUtil.wrap(short.class)).isSameAs(Short.class);
        assertThat(ClassUtil.wrap(void.class)).isSameAs(Void.class);
    }
}
