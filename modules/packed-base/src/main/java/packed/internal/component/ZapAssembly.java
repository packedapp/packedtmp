package packed.internal.component;

import app.packed.application.App;
import app.packed.container.BaseAssembly;
import app.packed.inject.Provide;

public class ZapAssembly extends BaseAssembly {

    @Override
    protected void build() {
        link(new LinkMe());
    }

    public static void main(String[] args) {
        App.driver().print(new ZapAssembly());
    }

    static class LinkMe extends BaseAssembly {

        @Override
        protected void build() {
            installInstance("SDADs");
            install(My.class);
        }
        
        @Provide 
        public String foo() {
            return "asdsad";
        }
    }

    static class My {}
}
