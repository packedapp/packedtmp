package app.packed.inject;

import app.packed.container.BaseBundle;

public class FTest extends BaseBundle {

    public FTest() {}

    FTest(String s) {}

    public static void main(String[] args) {
        Factory.find(FTest.class);
    }

    @Override
    protected void configure() {
        // Usage of Factory.Map...

        // Ideen er at vi ikke gider laver X flere gange...

        // providePrototype
        provide(new Factory2<Integer, Long, X>((a, b) -> new X(a, b)) {}.mapTo(String.class, x -> x.sum));
    }

}

class X {

    String sum; // Skal ikke vaere en sum....

    X(int i, long l) {
        sum = i + l + "";
    }
}

//People can make this themself...
@interface StaticInject {

}