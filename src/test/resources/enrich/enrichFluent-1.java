/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import javax.annotation.processing.Generated;
import java.util.Set;
import java.util.Map;
import java.util.List;

@Generated(value = "net.binis.codegen.TestPrototype", comments = "TestImpl")
public interface Test {
    List<Long> getList();
    Map<Long, String> getMap();
    Set<Long> getSet();
    String getTitle();

    Test list(List<Long> list);
    Test map(Map<Long, String> map);

    Test set(Set<Long> set);

    Test title(String title);
}
