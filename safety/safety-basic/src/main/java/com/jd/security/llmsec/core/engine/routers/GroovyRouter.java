package com.jd.security.llmsec.core.engine.routers;

import com.alibaba.fastjson2.JSON;
import com.google.common.base.Preconditions;
import com.jd.security.llmsec.core.ResponseCode;
import com.jd.security.llmsec.core.conf.RouterConfig;
import com.jd.security.llmsec.core.conf.RouterType;
import com.jd.security.llmsec.core.engine.Node;
import com.jd.security.llmsec.core.exception.ExceptionWithCode;
import com.jd.security.llmsec.core.session.SessionContext;
import com.jd.security.llmsec.core.util.GroovyHelper;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.IOException;
import java.util.Map;



public class GroovyRouter extends AbstractRouter {
    private static Logger logger = LoggerFactory.getLogger(GroovyRouter.class);
    /*
    https://groovy-lang.org/integrating.html
     */
    private final Map<String, Node> nodeMap;
    public GroovyRouter(RouterConfig routerConf, Map<String, Node> nodeMap) {
        super(routerConf, nodeMap);
        this.nodeMap = nodeMap;
    }

    @Override
    public Node next(SessionContext context) throws ExceptionWithCode {
        try {
            Object eval = null;
            synchronized (engine) {
                Bindings bindings = engine.createBindings();
                bindings.put("ctx", context);
                eval = engine.eval(conf.getScript(), bindings);
            }

            if (eval == null) {
                return null;
            }

            if (!(eval instanceof String)) {
                throw new ExceptionWithCode(ResponseCode.inner_error.code, "groovy route返回非字符串");
            }
            return nodeMap.get((String) eval);
        } catch (ScriptException e) {
            logger.error("执行groovy脚本失败, ctx={}, conf={}", JSON.toJSONString(context), JSON.toJSONString(conf), e);
        }
        return null;
    }

    @Override
    public RouterType type() {
        return RouterType.groovy;
    }

    @Data
    public static class Conf {
        private String script;
        private String scriptRaw;
    }

    private Conf conf;
    private ScriptEngine engine;
    @Override
    protected void doInit(Map<String, Object> conf, Map<String, Node> nodeMap) {
        this.conf = JSON.parseObject(JSON.toJSONString(conf), Conf.class);
        String script = this.conf.getScript();
        Preconditions.checkNotNull(script);
        this.conf.setScriptRaw(script);
        engine = GroovyHelper.engin(script);
    }
}
