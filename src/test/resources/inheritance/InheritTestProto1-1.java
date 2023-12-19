/*Generated code by Binis' code generator.*/
package net.binis.codegen.proto;

import net.binis.codegen.proto.base.InheirTest;
import javax.annotation.processing.Generated;

@Generated(value = "net.binis.codegen.proto.ParentPrototype", comments = "ParentImpl")
public interface Parent {
    int getData();
    InheirTest getParent();

    void setData(int data);
    void setParent(InheirTest parent);
}
