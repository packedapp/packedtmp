package app.packed.cli;

import java.util.function.Consumer;

import app.packed.application.App;
import app.packed.container.Assembly;
import app.packed.container.BaseAssembly;
import app.packed.container.Wirelet;
import app.packed.inject.Factory;

// Skal vi returner int???

// Ideen er det mere er en bootstrapper...

abstract class CliAssembly extends BaseAssembly {

    // Provides a result... men det goer main jo ikke...
    @SuppressWarnings("unused")
    private final void main(Factory<?> factory) {
        throw new UnsupportedOperationException();
    }

    protected final void onMain(Op<?> op) {
        throw new UnsupportedOperationException();
    }

    protected final void onMain(Runnable runnable) {
        throw new UnsupportedOperationException();
    }

    public static void main(Assembly<?> assembly, String[] args, Wirelet... wirelets) {
        App.run(assembly, wirelets);
    }

    public static void main(Assembly<?> assembly, Wirelet... wirelets) {
        App.run(assembly, wirelets);
    }

    public static abstract class Op<T> {

        public Op(Consumer<T> consumer) {

        }
    }
}
