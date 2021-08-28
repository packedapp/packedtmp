package app.packed.lifecycle;

import app.packed.application.programs.SomeApp;
import app.packed.container.BaseAssembly;

public class Tester extends BaseAssembly {

    @Override
    protected void build() {
        link(new MyM());
    }

    public static void main(String[] args) {
        SomeApp.driver().launch(new Tester());
    }
    
    static class MyM extends BaseAssembly {

        @Override
        protected void build() {
            new Exception().printStackTrace();
        }
    }
}
