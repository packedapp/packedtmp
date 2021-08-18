package app.packed.extension.sandbox.connect;

public class Xit {

    public static void main(String[] args) {
        System.out.println(ClassLoader.getPlatformClassLoader());
        System.out.println(ClassLoader.getSystemClassLoader());
        System.out.println(Xit.class.getClassLoader());
    }
}
