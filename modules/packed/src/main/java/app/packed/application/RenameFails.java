package app.packed.application;

import java.lang.annotation.Annotation;

public class RenameFails {

    final static ClassValue<RenameFails> STUFF = new ClassValue<>() {

        @Override
        protected RenameFails computeValue(Class<?> type) {
            for (Annotation a : type.getAnnotations()) {
                if (a instanceof Deprecated h) {}
            }
            return null;
        }
    };
}