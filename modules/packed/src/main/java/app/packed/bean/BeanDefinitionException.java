package app.packed.bean;

import app.packed.component.BuildException;

// Ideen er lidt at vi kan smide den her, hvis der er noget problemer med Bean definitionen
// F.eks. 2 injects...

// BeanDeclarationException???
public class BeanDefinitionException extends BuildException {

    /** */
    private static final long serialVersionUID = 1L;

    public BeanDefinitionException(String message) {
        super(message);
    }

}
