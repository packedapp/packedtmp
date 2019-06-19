package onclasspath;

import org.junit.jupiter.api.Test;

import app.packed.app.App;
import app.packed.component.ComponentExtension;
import app.packed.container.Bundle;

public class BasicTest {

  @Test
  public void test() {
      App.of(new TestModule());
      System.err.println("XXXXXXX MODULE");
      System.err.println(TestModule.class.getModule());
      throw new Error();
  }
}

class TestModule extends Bundle {

    public void configure() {
        install("foo");
        assert(extensions().contains(ComponentExtension.class));
    }
}
