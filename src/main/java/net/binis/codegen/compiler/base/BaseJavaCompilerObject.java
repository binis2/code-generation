package net.binis.codegen.compiler.base;

import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.factory.CodeFactory;

import javax.annotation.processing.ProcessingEnvironment;

import static java.util.Objects.isNull;
import static net.binis.codegen.generation.core.Helpers.lookup;
import static net.binis.codegen.tools.Reflection.invoke;

@Slf4j
public abstract class BaseJavaCompilerObject {

    protected final ProcessingEnvironment env;
    protected Object instance;
    protected Object context;
    protected Class cls;

    protected BaseJavaCompilerObject() {
        this.env = CodeFactory.create(ProcessingEnvironment.class, lookup.getProcessingEnvironment());
        context = invoke("getContext", env);
        if (isNull(context)) {
            log.error("Unable to get context from {}!", env.getClass());
        }
        init();
    }

    protected abstract void init();

    public Class getCls() {
        return cls;
    }

    public Object getInstance() {
        return instance;
    }


}
