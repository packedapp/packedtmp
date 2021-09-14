package app.packed.exceptionhandling;

import app.packed.bundle.Wirelet;

/**
 * Various wirelets that can be used to precise control how exceptions are handled for a component.
 */
public class ExceptionWirelets {

    // Overskriver alt andet... En lille smule inheritance vi godt kunne bruge
    public static Wirelet alwaysFail() {
        throw new UnsupportedOperationException();
    }
}
