package app.packed.service.dep2;

// Can never be exported...
// 
@interface ProvideLocal {
    boolean inherit() default false;
    // boolean
}
