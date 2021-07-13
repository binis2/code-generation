/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import java.util.Set;
import java.util.Map;
import java.util.List;

public interface Test {

    Test clone();

    List<Long> getList();

    Map<Long, String> getMap();

    Set<Long> getSet();

    String getTitle();

    void setList(List<Long> list);

    void setMap(Map<Long, String> map);

    void setSet(Set<Long> set);

    void setTitle(String title);
}
