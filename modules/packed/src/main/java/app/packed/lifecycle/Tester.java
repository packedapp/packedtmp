package app.packed.lifecycle;

import app.packed.application.App;
import app.packed.container.BaseAssembly;

public class Tester extends BaseAssembly {

    @Override
    protected void build() {
        link(new MyM());
    }

    public static void main(String[] args) {
        App.run(new Tester());
    }

    static class MyM extends BaseAssembly {

        @Override
        protected void build() {
            new Exception().printStackTrace();
        }
    }
}
