/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import java.util.stream.Collectors;
import java.util.Set;
import java.util.Map;
import java.util.List;

public class TestImpl implements Test {

    protected List<Long> list;

    protected Map<Long, String> map;

    protected Set<Long> set;

    protected String title;

    public TestImpl() {
    }

    public Test clone() {
        var result = new TestImpl();
        result.title = title;
        if (list != null) {
            result.list = list.stream().collect(Collectors.toList());
        }
        if (set != null) {
            result.set = set.stream().collect(Collectors.toSet());
        }
        if (map != null) {
            result.map = map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        return result;
    }

    public List<Long> getList() {
        return list;
    }

    public Map<Long, String> getMap() {
        return map;
    }

    public Set<Long> getSet() {
        return set;
    }

    public String getTitle() {
        return title;
    }

    public void setList(List<Long> list) {
        this.list = list;
    }

    public void setMap(Map<Long, String> map) {
        this.map = map;
    }

    public void setSet(Set<Long> set) {
        this.set = set;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
