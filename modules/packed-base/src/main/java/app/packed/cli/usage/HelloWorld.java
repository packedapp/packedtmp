package app.packed.cli.usage;

import java.lang.invoke.MethodType;

import app.packed.application.AppImage;
import app.packed.container.BaseAssembly;

public class HelloWorld extends BaseAssembly {

    private static final AppImage MAIN = AppImage.of(new HelloWorld());

    protected void build() {
        provide(Foo.class);
        provide(Boo.class);
    }

    public static void main(String[] args) {
        MethodType mt = MethodType.genericMethodType(12);
        System.out.println(mt.returnType());
        MAIN.use(args);
    }

    public static class Foo {
        public Foo() {
            System.out.println("OK");
        }
    }
    
    public static class Boo {
        public Boo(Foo ffooo) {
            System.out.println("OK");
        }
    }
}
