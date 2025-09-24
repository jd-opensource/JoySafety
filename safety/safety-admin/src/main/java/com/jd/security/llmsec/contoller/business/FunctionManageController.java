package com.jd.security.llmsec.contoller.business;

import com.google.common.base.Preconditions;
import com.jd.security.llmsec.core.ResponseMessage;
import com.jd.security.llmsec.pojo.business.FunctionConfVO;
import com.jd.security.llmsec.service.business.DefenseConfigService;
import com.jd.security.llmsec.service.business.FunctionConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;



@RestController
@RequestMapping("/config/defense/manage/function")
public class FunctionManageController {
    private Logger logger = LoggerFactory.getLogger(FunctionManageController.class);

    @Autowired
    private FunctionConfigService functionConfigService;

    @Autowired
    private DefenseConfigService defenseConfigService;

    /*
    function变更：
    新增流程：新增 => 上线 => 下线
    更新流程：获取 => 新版本（带更新） => 更新（可以0次或者多次） => 功能验证  => 升级
     */

    // 新加
    @PostMapping("new")
    public Object newFunction(@RequestBody FunctionConfVO functionConf) {
        try {
            FunctionConfVO added = functionConfigService.newFunction(functionConf);
            logger.error("添加function成功, req={}", functionConf);
            return ResponseMessage.success("success", added);
        } catch (Exception e) {
            logger.error("添加function失败, req={}", functionConf, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    // 获取信息
    @PostMapping("get")
    public Object getFunction(@RequestBody FunctionConfVO functionConf) {
        try {
//            Preconditions.checkArgument(functionConf.getId() != null, "id不能为空");
            FunctionConfVO functionInfo = functionConfigService.getFunction(functionConf.getId());
            logger.error("下线function成功, req={}", functionConf);
            return ResponseMessage.success("success", functionInfo);
        } catch (Exception e) {
            logger.error("下线function失败, req={}", functionConf, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    // 新加版本
    @PostMapping("newVersion")
    public Object newFunctionVersion(@RequestBody FunctionConfVO functionConf) {
        try {
            FunctionConfVO added = functionConfigService.newFunctionVersion(functionConf);
            logger.error("添加function成功, req={}", functionConf);
            return ResponseMessage.success("success", added);
        } catch (Exception e) {
            logger.error("添加function失败, req={}", functionConf, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    // 指定版本(不能是online版本)更新
    @PostMapping("update")
    public Object updateFunction(@RequestBody FunctionConfVO functionConf) {
        try {
            Preconditions.checkArgument(functionConf.getId() != null, "id不能为空");
            Preconditions.checkArgument(functionConf.getVersion() != null, "version不能为空");
            FunctionConfVO updated = functionConfigService.updateFunction(functionConf);
            logger.error("升级function成功, req={}", functionConf);
            return ResponseMessage.success("success", updated);
        } catch (Exception e) {
            logger.error("升级function失败, req={}", functionConf, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    // 上线
    @PostMapping("online")
    public Object functionOnline(@RequestBody FunctionConfVO functionConf) {
        try {
            Preconditions.checkArgument(functionConf.getId() != null, "id不能为空");
            FunctionConfVO updated = functionConfigService.functionOnline(functionConf.getId());
            logger.error("上线function成功, req={}", functionConf);
            return ResponseMessage.success("success", updated);
        } catch (Exception e) {
            logger.error("上线function失败, req={}", functionConf, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    // 下线
    @PostMapping("offline")
    public Object functionOffline(@RequestBody FunctionConfVO functionConf) {
        try {
            FunctionConfVO updated = defenseConfigService.functionOffline(functionConf.getId());
            logger.error("下线function成功, req={}", functionConf);
            return ResponseMessage.success("success", updated);
        } catch (Exception e) {
            logger.error("下线function失败, req={}", functionConf, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    @PostMapping("upgrade")
    public Object functionUpgrade(@RequestBody FunctionConfVO functionConf) {
        try {
            FunctionConfVO updated = functionConfigService.upgradeFunction(functionConf);
            logger.error("升级function成功, req={}", functionConf);
            return ResponseMessage.success("success", updated);
        } catch (Exception e) {
            logger.error("升级function失败, req={}", functionConf, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    @PostMapping("active")
    public Object functionActive(@RequestBody FunctionConfVO functionConf) {
        try {
            FunctionConfVO vo = functionConfigService.activeConf(functionConf.getGroup(), functionConf.getName());
            return ResponseMessage.success("success", vo);
        } catch (Exception e) {
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    @PostMapping("allActive")
    public Object functionAllActive() {
        try {
            List<FunctionConfVO> vos = functionConfigService.allActiveConf();
            return ResponseMessage.success("success", vos);
        } catch (Exception e) {
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }
}
