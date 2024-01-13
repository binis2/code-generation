/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import net.binis.codegen.tools.CollectionUtils;
import javax.annotation.processing.Generated;
import java.util.Set;
import java.util.Map;
import java.util.List;

@Generated(value = "net.binis.codegen.TestPrototype", comments = "Test")
public class TestImpl extends BaseImpl implements Test {

    protected List<Long> list;

    protected Map<Long, String> map;

    protected Set<Long> set;

    protected String title;

    public TestImpl() {
        super();
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

    @Override()
    public String toString() {
        return "Test(title = " + title + ", list = " + CollectionUtils.printInfo(list, true) + ", set = " + CollectionUtils.printInfo(set, true) + ", map = " + CollectionUtils.printInfo(map, true) + ", " + ")";
    }
}
