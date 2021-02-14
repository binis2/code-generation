/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.factory.CodeFactory;

public class TestImpl extends BaseImpl implements Test {

    protected String title;

    {
        CodeFactory.registerType(Test.class, TestImpl::new, null);
    }

    public TestImpl() {
        super();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
