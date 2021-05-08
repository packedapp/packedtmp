package app.packed.exceptionhandling;

import java.util.Optional;

import app.packed.attribute.AttributedElement;
import app.packed.component.ComponentMirror;
import app.packed.container.Extension;

public interface FailureContext extends AttributedElement {

    boolean isRetryable();

    // If it is an extension that failed...
    Optional<Extension> failingExtension();

    ComponentMirror component(); // vs instance Hmm begge ting giver mening!>!~

    Throwable cause();
}
// DefaultAction