/*Generated code by Binis' code generator.*/
package net.binis.codegen.test;

import net.binis.codegen.objects.DefaultPayload;
import net.binis.codegen.modifier.Modifiable;
import net.binis.codegen.factory.CodeFactory;
import javax.annotation.processing.Generated;

@Generated(value = "TestPrototype", comments = "Test")
public class TestImpl implements Test, Modifiable<Test.Modify> {

    protected DefaultPayload payload = null;

    // region constructor & initializer
    {
        CodeFactory.registerType(Test.class, TestImpl::new, null);
    }

    public TestImpl() {
    }
    // endregion

    // region getters
    public DefaultPayload getPayload() {
        return payload;
    }
    // endregion

    // region setters
    public void setPayload(DefaultPayload payload) {
        this.payload = payload;
    }

    public Test.Modify with() {
        return new TestModifyImpl();
    }
    // endregion

    // region inner classes
    protected class TestModifyImpl implements Test.Modify {

        public Test done() {
            return TestImpl.this;
        }

        public Test.Modify payload(DefaultPayload payload) {
            TestImpl.this.payload = payload;
            return this;
        }
    }
    // endregion
}
