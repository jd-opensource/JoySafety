package com.jd.security.llmsec.core.util;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


public class GroovyHelper {
    public static ScriptEngineManager factory = new ScriptEngineManager();
    public static ScriptEngine engin() {
        return factory.getEngineByName("groovy");
    }

    public static ScriptEngine engin(String script) {
        ScriptEngine engine = factory.getEngineByName("groovy");
        doWarmup(engine, script);
        return engine;
    }

    private static void doWarmup(ScriptEngine engine, String script) {
        Bindings bindings = engine.createBindings();
        try {
            engine.eval(script, bindings);
        } catch (ScriptException e) {
            // ignore
        }
    }
}
