package packed.internal.component;

import app.packed.application.Main;
import app.packed.container.BaseAssembly;

public class ZapAssembly extends BaseAssembly {

    @Override
    protected void build() {
        link(new LinkMe());
        //throw new Error();
    }

    public static void main(String[] args) {
        Main.run(new ZapAssembly(), new InternalWirelet.FailOnFirstPass());
    }

    static class LinkMe extends BaseAssembly {

        @Override
        protected void build() {
            throw new Error();
        }
    }
}
