/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import javax.annotation.processing.Generated;
import java.util.function.Function;

@Generated(value = "TestPrototype", comments = "TestImpl")
public interface Test {

    String getTitle();

    void setTitle(String title);

    Test.Modify with();

    interface Fields<T> {

        T title(String title);
    }

    interface Modify extends Test.Fields<Test.Modify> {

        net.binis.codegen.spring.async.AsyncModifier<Test.Modify> async();

        Test delete();

        Test done();

        Test merge();

        Test refresh();

        Test save();

        Test saveAndFlush();

        Test transaction(Function<Test.Modify, Test> function);
    }
}
