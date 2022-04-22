/*Generated code by Binis' code generator.*/
package net.binis.codegen.test;

import net.binis.codegen.objects.DefaultPayload;
import net.binis.codegen.modifier.Modifiable;
import net.binis.codegen.factory.CodeFactory;
import javax.annotation.processing.Generated;

@Generated(value = "TestPrototype", comments = "Test")
public class TestImpl extends GenericImpl<DefaultPayload> implements Test, Modifiable<Test.Modify> {

    {
        CodeFactory.registerType(Test.class, TestImpl::new, null);
    }

    public TestImpl() {
        super();
    }

    public Test.Modify with() {
        return new TestModifyImpl();
    }

    protected class TestModifyImpl implements Test.Modify {

        public Test done() {
            return TestImpl.this;
        }

        public Test.Modify payload(DefaultPayload payload) {
            TestImpl.this.payload = payload;
            return this;
        }
    }
}
