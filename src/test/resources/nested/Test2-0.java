/*Generated code by Binis' code generator.*/
package net.binis.codegen.test.objects;

import net.binis.codegen.factory.CodeFactory;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.test.objects.TestRequestPrototype", comments = "TestRequest")
public class TestRequestImpl implements TestRequest {

    protected String name;

    protected String numbers;

    protected TestRequest.SubRequest sub;

    protected String value;

    // region constructor & initializer
    {
        CodeFactory.registerType(TestRequest.class, TestRequestImpl::new, null);
    }

    public TestRequestImpl() {
    }
    // endregion

    // region getters
    public String getName() {
        return name;
    }

    public String getNumbers() {
        return numbers;
    }

    public TestRequest.SubRequest getSub() {
        return sub;
    }

    public String getValue() {
        return value;
    }
    // endregion

    // region inner classes
    public static class SubRequestImpl implements SubRequest {

        protected String value;

        // region constructor & initializer
        {
            CodeFactory.registerType(TestRequest.SubRequest.class, SubRequestImpl::new, null);
        }

        public SubRequestImpl() {
        }
        // endregion

        // region getters
        public String getValue() {
            return value;
        }
        // endregion
    }
    // endregion
}
