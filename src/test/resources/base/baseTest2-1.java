/*Generated code by Binis' code generator.*/
package net.binis.codegen;

public interface Test extends Base {

    String getTitle();

    void setTitle(String title);

    Test.Modify with();

    interface Modify {

        Test done();

        Modify id(Long id);

        Modify title(String title);
    }
}
