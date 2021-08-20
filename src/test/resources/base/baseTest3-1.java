/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import javax.annotation.processing.Generated;

@Generated(value = "TestPrototype", comments = "TestImpl")
public interface Test extends Base {

    String getTitle();

    void setTitle(String title);

    Test.Modify with();

    interface Fields<T> extends Base.Fields<T> {

        T title(String title);
    }

    interface Modify extends Test.Fields<Test.Modify> {

        Test done();
    }
}
