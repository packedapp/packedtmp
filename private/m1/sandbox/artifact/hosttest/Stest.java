/*
 * Copyright (c) 2008 Kasper Nielsen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sandbox.artifact.hosttest;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import app.packed.artifact.App;
import app.packed.container.ContainerBundle;
import app.packed.container.Extension;

/**
 *
 */
public class Stest extends ContainerBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        use(WebExtension.class).get("/functional", "Hello Functional!");
        install(SomeComponent.class);
        extensions().forEach(e -> System.out.println(e));
    }

    public static void main(String[] args) {
        App.main(new Stest(), args);
    }

    static class SomeComponent {

        @Get("/annotation")
        public String get() {
            return "Hello Annotations!";
        }
    }
}

//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@SpringBootApplication
//@RestController
//public class SpringbootApplication {
//
//  public static void main(String[] args) {
//    SpringApplication.run(SpringbootApplication.class, args);
//  }
//
//  @GetMapping("/")
//  public String hello() {
//    return "Hello world!";
//  }
//
//}

class SomeTest {

    // ideen var at injecte X fra containeren...
    <X, Y> Consumer<Y> inj(Class<X> x, BiConsumer<X, Y> consumer) {
        throw new UnsupportedOperationException();
    }
}

class WebExtension extends Extension {

    /**
     * @param string
     * @param string2
     */
    public void get(String string, String string2) {}
}

@interface Get {
    String value();
}