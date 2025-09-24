package com.jd.security.llmsec.contoller.business;

import com.alibaba.fastjson2.JSON;
import com.google.common.base.Preconditions;
import com.jd.security.llmsec.core.ResponseMessage;
import com.jd.security.llmsec.data.pojo.BusinessInfoWithBLOBs;
import com.jd.security.llmsec.pojo.business.BusinessInfoVO;
import com.jd.security.llmsec.service.business.BusinessConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;



@RestController
@RequestMapping("/config/defense/manage/business")
public class BusinessManageController {
    // todo 业务名，group，一旦确定，不能更新
    private Logger logger = LoggerFactory.getLogger(BusinessManageController.class);

    @Autowired
    @Qualifier("BusinessConfigServiceImpl")
    private BusinessConfigService businessConfigService;

    /*
    新增流程：新增 => 上线 => 下线
    更新流程：获取 => 新版本（带更新） => 更新（可以0次或者多次） => 功能验证  => 升级
     */

    // 新加
    @PostMapping("new")
    public Object newBusiness(@RequestBody BusinessInfoVO businessConf) {
        try {
            BusinessInfoVO added = businessConfigService.newBusiness(businessConf);
            logger.info("添加business成功, req={}", businessConf);
            return ResponseMessage.success("success", added);
        } catch (Exception e) {
            logger.error("添加business失败, req={}", businessConf, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    // 获取信息
    @PostMapping("get")
    public Object getBusiness(@RequestBody BusinessInfoVO businessConf) {
        try {
            BusinessInfoVO businessInfo = businessConfigService.getBusiness(businessConf.getId());
            logger.info("下线business成功, req={}", businessConf);
            return ResponseMessage.success("success", businessInfo);
        } catch (Exception e) {
            logger.error("下线business失败, req={}", businessConf, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    @PostMapping("listAllVersions")
    public Object listAllVersions(@RequestBody BusinessInfoVO businessConf) {
        try {
            List<BusinessInfoVO> businessInfo = businessConfigService.listAllVersions(businessConf);
            logger.info("获取所有版本成功, req={}", businessConf);
            return ResponseMessage.success("success", businessInfo);
        } catch (Exception e) {
            logger.error("获取所有版本失败, req={}", JSON.toJSONString(businessConf), e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    // 新加版本
    @PostMapping("newVersion")
    public Object newBusinessVersion(@RequestBody BusinessInfoVO businessConf) {
        try {
            BusinessInfoVO added = businessConfigService.newBusinessVersion(businessConf);
            logger.info("添加business成功, req={}", businessConf);
            return ResponseMessage.success("success", added);
        } catch (Exception e) {
            logger.error("添加business失败, req={}", businessConf, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    // 指定版本(不能是online版本)更新
    @PostMapping("update")
    public Object updateBusiness(@RequestBody BusinessInfoVO businessConf) {
        try {
            Preconditions.checkArgument(businessConf.getId() != null, "id不能为空");
            Preconditions.checkArgument(businessConf.getVersion() != null, "version不能为空");
            BusinessInfoVO updated = businessConfigService.updateBusiness(businessConf);
            logger.info("升级business成功, req={}", businessConf);
            return ResponseMessage.success("success", updated);
        } catch (Exception e) {
            logger.error("升级business失败, req={}", businessConf, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    @PostMapping("upgrade")
    public Object upgradeBusiness(@RequestBody BusinessInfoVO businessConf) {
        try {
            Preconditions.checkArgument(businessConf.getId() != null, "id不能为空");
            BusinessInfoVO updated = businessConfigService.upgradeBusiness(businessConf);
            logger.info("升级business成功, req={}", businessConf);
            return ResponseMessage.success("success", updated);
        } catch (Exception e) {
            logger.error("升级business失败, req={}", businessConf, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    // 上线
    @PostMapping("online")
    public Object businessOnline(@RequestBody BusinessInfoWithBLOBs businessConf) {
        try {
            BusinessInfoVO updated = businessConfigService.businessOnline(businessConf.getId());
            logger.info("上线business成功, req={}", businessConf);
            return ResponseMessage.success("success", updated);
        } catch (Exception e) {
            logger.error("上线business失败, req={}", businessConf, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    // 下线
    @PostMapping("offline")
    public Object businessOffline(@RequestBody BusinessInfoWithBLOBs businessConf) {
        try {
            BusinessInfoVO updated = businessConfigService.businessOffline(businessConf.getId());
            logger.info("下线business成功, req={}", businessConf);
            return ResponseMessage.success("success", updated);
        } catch (Exception e) {
            logger.error("下线business失败, req={}", businessConf, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    @PostMapping("active")
    public Object businessActive(@RequestBody BusinessInfoWithBLOBs businessConf) {
        try {
            BusinessInfoVO record = businessConfigService.activeConf(businessConf.getGroup(), businessConf.getName());
            return ResponseMessage.success("success", record);
        } catch (Exception e) {
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    @PostMapping("allActive")
    public Object businessAllActive() {
        try {
            List<BusinessInfoVO> record = businessConfigService.allActiveConf();
            return ResponseMessage.success("success", record);
        } catch (Exception e) {
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }
}
