/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import java.util.function.Function;

public interface Test {

    String getTitle();

    void setTitle(String title);

    Test.Modify with();

    interface Fields<T> {

        T title(String title);
    }

    interface Modify extends Test.Fields<Test.Modify> {

        Test delete();

        Test done();

        Test merge();

        Test refresh();

        Test save();

        Test saveAndFlush();

        Test transaction(Function<Test.Modify, Test> function);
    }
}