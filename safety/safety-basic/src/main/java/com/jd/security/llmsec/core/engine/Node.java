package com.jd.security.llmsec.core.engine;

import com.jd.security.llmsec.core.check.RiskCheckResult;
import com.jd.security.llmsec.core.conf.NodeConfig;
import com.jd.security.llmsec.core.exception.ExceptionWithCode;
import com.jd.security.llmsec.core.session.SessionContext;
import com.jd.security.llmsec.core.util.GroovyHelper;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.IOException;



@Data
public class Node {
    private NodeConfig conf;
    private Function function;
    private ScriptEngine reduceEngine = null;
    private Router router;

    private static final Logger logger = LoggerFactory.getLogger(Node.class);

    public Node(NodeConfig conf) throws IOException {
        this.conf = conf;
        String reduceScript = conf.getReduceScript();
        if (StringUtils.isEmpty(reduceScript)) {
            return;
        }
        conf.setReduceScriptRaw(reduceScript);

        this.reduceEngine = GroovyHelper.engin(reduceScript);
    }

    public RiskCheckResult run(SessionContext context) throws ExceptionWithCode {
        RiskCheckResult curResult = function.run(context);
        if (reduceEngine == null) {
            return curResult;
        }

        synchronized (reduceEngine) {
            Bindings bindings = reduceEngine.createBindings();
            bindings.put("ctx", context);
            bindings.put("curNode", this);
            bindings.put("curResult", curResult);
            try {
                return (RiskCheckResult) reduceEngine.eval(conf.getReduceScript(), bindings);
            } catch (ScriptException e) {
                logger.error("执行脚本失败，忽略脚本中的策略", e);
                return curResult;
            }
        }
    }

    public Node next(SessionContext context) throws ExceptionWithCode {
        if (router == null) {
            return null;
        } else {
            return router.next(context);
        }
    }
}
