/*Generated code by Binis' code generator.*/
package net.binis.codegen.proto2;

import net.binis.codegen.proto.Parent;
import javax.annotation.processing.Generated;
import java.util.List;

@Generated(value = "net.binis.codegen.proto2.Parent2Prototype", comments = "Parent2Impl")
public interface Parent2 extends Parent {
    List<String> getList();
    String getTitle();

    void setList(List<String> list);
    void setTitle(String title);
}
