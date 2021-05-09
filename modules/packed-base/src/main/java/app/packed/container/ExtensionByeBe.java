package app.packed.container;

import app.packed.application.App;

public class ExtensionByeBe extends BaseAssembly {

    @Override
    protected void build() {
        provideInstance("dsoo").export();

    }

    public static void main(String[] args) {
        
        App.driver().print(new ExtensionByeBe());

    }

}
