/*Generated code by Binis' code generator.*/
package net.binis.codegen.test.objects;

import net.binis.codegen.annotation.Default;
import javax.annotation.processing.Generated;

@Generated(value = "TestRequestPrototype", comments = "TestRequestImpl")
@Default("net.binis.codegen.test.objects.TestRequestImpl")
public interface TestRequest {
    String getName();
    String getNumbers();
    TestRequest.SubRequest getSub();
    String getValue();

    // region inner classes
    @Default("net.binis.codegen.test.objects.TestRequestImpl$SubRequestImpl")
    public interface SubRequest {
        String getValue();
    }
    // endregion
}
