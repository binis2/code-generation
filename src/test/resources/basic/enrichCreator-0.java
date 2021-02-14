/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.factory.CodeFactory;

public class TestImpl implements Test {

    protected String title;

    {
        CodeFactory.registerType(Test.class, TestImpl::new, null);
    }

    public TestImpl() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
