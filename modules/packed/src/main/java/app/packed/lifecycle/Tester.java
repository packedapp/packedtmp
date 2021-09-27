package app.packed.lifecycle;

import app.packed.application.programs.SomeApp;
import app.packed.bundle.BaseBundle;

public class Tester extends BaseBundle {

    @Override
    protected void build() {
        link(new MyM());
    }

    public static void main(String[] args) {
        SomeApp.run(new Tester());
    }
    
    static class MyM extends BaseBundle {

        @Override
        protected void build() {
            new Exception().printStackTrace();
        }
    }
}
