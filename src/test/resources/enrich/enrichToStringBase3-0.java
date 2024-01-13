/*Generated code by Binis' code generator.*/
package net.binis.codegen;

import javax.annotation.processing.Generated;
import java.util.Set;
import java.util.Map;
import java.util.List;

@Generated(value = "net.binis.codegen.BasePrototype", comments = "Base")
public class BaseImpl implements Base {

    protected List<Long> baseList;

    protected Map<Long, String> baseMap;

    protected Set<Long> baseSet;

    protected String baseTitle;

    public BaseImpl() {
    }

    public List<Long> getBaseList() {
        return baseList;
    }

    public Map<Long, String> getBaseMap() {
        return baseMap;
    }

    public Set<Long> getBaseSet() {
        return baseSet;
    }

    public String getBaseTitle() {
        return baseTitle;
    }

    public void setBaseList(List<Long> baseList) {
        this.baseList = baseList;
    }

    public void setBaseMap(Map<Long, String> baseMap) {
        this.baseMap = baseMap;
    }

    public void setBaseSet(Set<Long> baseSet) {
        this.baseSet = baseSet;
    }

    public void setBaseTitle(String baseTitle) {
        this.baseTitle = baseTitle;
    }

    @Override()
    public String toString() {
        return "Base(" + ")";
    }
}
