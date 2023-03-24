package net.binis.codegen.compiler;

import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.compiler.base.BaseJavaCompilerObject;

import java.util.HashMap;
import java.util.Map;

import static net.binis.codegen.tools.Reflection.*;

@Slf4j
public class CGTypeTag extends BaseJavaCompilerObject {

    public static Class theClass() {
        return loadClass("com.sun.tools.javac.code.TypeTag");
    }

    protected static Map<String, Object> initValues() {
        var result = new HashMap<String, Object>();
        var cls = theClass();
        for (var e : cls.getEnumConstants()) {
            result.put(e.toString(), e);
        }
        return result;
    }

    protected static Map<String, Object> values = initValues();

    public static final CGTypeTag CLASS = new CGTypeTag(values.get("CLASS"));


    public CGTypeTag(Object instance) {
        super();
        this.instance = instance;
    }

    @Override
    protected void init() {
        cls = theClass();
    }

}
