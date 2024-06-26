/*Generated code by Binis' code generator.*/
package net.binis.codegen.other;

import net.binis.codegen.Test;
import javax.annotation.processing.Generated;
import java.util.Optional;
import java.util.List;

@Generated(value = "net.binis.codegen.other.ReferencePrototype", comments = "ReferenceImpl")
public interface Reference {
    Optional<Test> getOptional();
    public List<Test> getSorted(List<Test> list);
    String getStr(Test proto);
    String getTitle();

    void setTitle(String title);
}
