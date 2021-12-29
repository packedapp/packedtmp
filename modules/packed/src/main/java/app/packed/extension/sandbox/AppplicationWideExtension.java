package app.packed.extension.sandbox;

import app.packed.extension.Extension;

// Ideen er lidt at man kan saette saadan en faetter op...
// Hvor der bliver lavet et per applikation...

// Protected static class paa Extension???

// Merger???

// Maaske er det kun saadan en faetter der kan installere applikation wide
// extension runtimes??? Og ikke extension selv?

// Maaske kan man injecte den her i hooks??


// Er det lidt extensionTree igen
abstract class AppplicationWideExtension<T extends Extension<?>> {

    protected final int count() {
        return 0;
    }

    boolean hasSingleRoot() {
        return false;
    }

    protected void onExtensionInit(T extension) {}

    protected void onExtensionComplete(T extension) {}
    
    protected void onComplete() {}
}
