package packed.internal.thirdparty.guice;

public class Ccc {

    public static void main(String[] args) {
        ClassValue<String> cv = new ClassValue<String>() {

            @Override
            protected String computeValue(Class<?> type) {
                // TODO Auto-generated method stub
                return null;
            }
        };
        System.out.println(cv.get(String.class));
    }
}
