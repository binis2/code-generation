/*Generated code by Binis' code generator.*/
package net.binis.codegen;

public interface Test extends Base {

    String getTitle();

    void setTitle(String title);

    Test.Modify with();

    interface Fields<T> {

        T id(Long id);

        T title(String title);
    }

    interface Modify extends Test.Fields<Test.Modify> {

        Test done();
    }
}
