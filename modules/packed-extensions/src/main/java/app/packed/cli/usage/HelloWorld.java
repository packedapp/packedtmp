package app.packed.cli.usage;

import java.lang.invoke.MethodType;

import app.packed.application.programs.SomeAppImage;
import app.packed.container.BaseBundle;

public class HelloWorld extends BaseBundle {

    private static final SomeAppImage MAIN = SomeAppImage.of(new HelloWorld());

    @Override
    protected void build() {
        provide(Foo.class);
        provide(Boo.class);
    }

    public static void main(String[] args) {
        MethodType mt = MethodType.genericMethodType(12);
        System.out.println(mt.returnType());
        MAIN.use();
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
