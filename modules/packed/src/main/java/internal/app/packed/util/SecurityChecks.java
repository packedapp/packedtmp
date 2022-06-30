package internal.app.packed.util;

import java.lang.reflect.Modifier;

public final class SecurityChecks {


    /**
     * Tests whether two classes are in the same package.
     * 
     * @param c1
     *            the first class
     * @param c2
     *            the second class.
     * @return true if the two classes are in the same package, otherwise null
     */
    public static boolean isClassesInSamePackage(Class<?> c1, Class<?> c2) {
        return (c1 == c2) || (c1.getClassLoader() == c2.getClassLoader() && c1.getPackageName().equals(c2.getPackageName()));
    }

    public static boolean isSuperInterfaceOf(Class<?> clazz, Class<?> interfaze) {
        for (Class<?> i : clazz.getInterfaces()) {
            if (i == interfaze || isSuperInterfaceOf(i, interfaze)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether or not the specified class is restricted.
     * 
     * @param clazz
     *            the class to test
     * @return whether or not the specified class is restricted
     */
    public static boolean isRestrictedClass(Class<?> clazz) {
        if (!Modifier.isPublic(clazz.getModifiers())) {
            return true; // Non public classes are always restricted
        }

        String packageName = clazz.getPackageName();
        if (packageName.length() == 0) {
            return false; // Classes in the default package are never restricted
        }

        // getModule is only null if called before the module system is initialized.
        // Which never happens in user code
        if (!clazz.getModule().isExported(packageName)) {
            return true; // classes that are not unconditional exported is always restricted
        }

        return false;
    }
}