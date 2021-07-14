package app.packed.application.host;

import app.packed.application.ApplicationMirror;
import app.packed.application.ApplicationRuntimeExtension;
import app.packed.component.Assembly;
import app.packed.component.Wirelet;
import app.packed.extension.Extension;

// Eneste problem er at vi skal aktivere den inde vi begynder at linke
// Det er nu en pris jeg er klar til at betale...
// Der er saa meget coolere...

// Saa er det bare en almindelige bean... Der haandtere komplicere
public class ApplicationHostExtension extends Extension {

    static {
        // skal vi have noget a.la. requires??? som automatisk installere ApplicationRuntimeException
        $dependsOnOptionally(ApplicationRuntimeExtension.class);
        
        
        //MethodHandles.classData(MethodHandles.lookup(), name, type)
    }

    ApplicationHostExtension() {}

    public ApplicationMirror delayedInitialization(Assembly<?> assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }
    
    // Ideen er at f.eks. vi har en specifik Session extension?
    // Evt. Session Application Assembly
    public class Sub extends Subtension {
        
    }
    
    // Som default linker man altid trustet ting...
}
