/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import javax.annotation.processing.Generated;
import java.util.Set;
import java.util.Map;
import java.util.List;

@Generated(value = "net.binis.codegen.BasePrototype", comments = "BaseImpl")
public interface Base {
    List<Long> getBaseList();
    Map<Long, String> getBaseMap();
    Set<Long> getBaseSet();
    String getBaseTitle();

    void setBaseList(List<Long> baseList);
    void setBaseMap(Map<Long, String> baseMap);
    void setBaseSet(Set<Long> baseSet);
    void setBaseTitle(String baseTitle);
}
