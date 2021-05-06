package app.packed.cli;

import app.packed.application.ApplicationDriver;
import app.packed.container.BaseAssembly;

// Maaske er det en application der udelukkende starter andre applicationer...
// Maaske er det ikke en gang en application... Jo, fordi man skal kunne lave
// 
interface Cli {

    // information omkring hvordan det er gaaet.. alt efter Exception handling

    static Cli main(String... args) {
        
        return null;
    }

    static ApplicationDriver<Cli> driver() {
        throw new UnsupportedOperationException();
    }
}

class MyCli extends BaseAssembly {

    @Override
    protected void build() {
        installInstance(this);
    }

    public void onDdd() {
       
    }

    public static void main(String[] args) {
        Cli.main(args);
    }
}