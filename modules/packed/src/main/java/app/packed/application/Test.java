package app.packed.application;
class Test {
    public int aaa() {
        int x = 1;
 
        try {
            return ++x;
        } catch (Exception e) {
 
        } finally {
            ++x;
        }
        return x;
    }
 
    public static void main(String[] args) {
        Test t = new Test();
        int y = t.aaa();
        System.out.println(y);
    }
}
