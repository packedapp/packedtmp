package zandbox.internal.hooks2.other;

import app.packed.inject.service.Provide;
import app.packed.inject.service.ServiceExtension;
import zandbox.packed.hooks.AccessibleFieldHook;
import zandbox.packed.hooks.ExtendWith;

@ExtendWith(accessibleFields = {
        @AccessibleFieldHook(onAnnotation = Provide.class, extension = ServiceExtension.class, bootstrapBean = AccessibleFieldHook.Bootstrap.class),
        @AccessibleFieldHook(bootstrapBean = AccessibleFieldHook.Bootstrap.class, extension = ServiceExtension.class, onAnnotation = Provide.class) })

// beanBinder.addAccessibleFieldHook(annotation, extension, bootstrap);
//// Eneste der taeller imod det med at smide den paa beans...
//// Er at man saa ikke kan extende den med andre annoteringer...
//// F.eks. 
public class TestIt {

    public static void main(String[] args) {
        ClassValue<String> cv = new ClassValue<String>() {

            @Override
            protected String computeValue(Class<?> type) {
                // TODO Auto-generated method stub
                return null;
            }
        };
        System.out.println(cv);
      //  System.out.println(cv.get(String.class));
    }
}
