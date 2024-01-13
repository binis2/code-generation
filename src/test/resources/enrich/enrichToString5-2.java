package net.binis.codegen;

import net.binis.codegen.test.TestExecutor;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;

@SuppressWarnings("unchecked")
public class Execute extends TestExecutor {

    @Override
    public boolean execute() {

        var s = new TestImpl();
        s.setTitle("title");
        s.setBaseTitle("baseTitle");
        s.setList(List.of(4L, 5L, 6L));
        s.setBaseList(new ArrayList<>(List.of(1L, 2L, 3L)));
        var set = new LinkedHashSet();
        set.add(4L);
        set.add(5L);
        set.add(6L);
        s.setSet(set);
        set = new LinkedHashSet();
        set.add(1L);
        set.add(2L);
        set.add(3L);
        s.setBaseSet(set);
        var map = new LinkedHashMap();
        map.put(4L, "4");
        map.put(5L, "5");
        map.put(6L, "6");
        s.setMap(map);
        map = new LinkedHashMap();
        map.put(1L, "1");
        map.put(2L, "2");
        map.put(3L, "3");
        s.setBaseMap(map);
        assertEquals("Test(baseList = ArrayList[3], baseSet = LinkedHashSet[3])", s.toString());

        return true;
    }
}
