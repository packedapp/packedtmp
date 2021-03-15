package app.packed.component;

// Det gode ved den her er at den jo samtidig fungere som
// en optional faetter
// WireletContainer/WireletHolder/WireletBag
// @DynamicInject
interface WireletHandle<T extends Wirelet> {
    T take(); // one() maybe. Emphasize at man consumer en...
}

class MyWirelet extends Wirelet {
    final String val = "asasd";
}

class Usage {

    public void foo(WireletHandle<MyWirelet> w) {
        w.take();
    }
}