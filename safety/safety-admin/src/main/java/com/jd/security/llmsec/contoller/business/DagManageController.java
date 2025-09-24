package com.jd.security.llmsec.contoller.business;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.base.Preconditions;
import com.jd.security.llmsec.config.YamlConfig;
import com.jd.security.llmsec.core.ResponseMessage;
import com.jd.security.llmsec.pojo.business.ExecuteDagConfVO;
import com.jd.security.llmsec.service.business.DagConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;



@RestController
@RequestMapping("/config/defense/manage/dag")
public class DagManageController {
    private Logger logger = LoggerFactory.getLogger(DagManageController.class);

    @Autowired
    @Qualifier("DagConfigServiceImpl")
    private DagConfigService dagConfigService;

    /*
    新增流程：新增 => 上线 => 下线
    更新流程：获取 => 新版本（带更新） => 更新（可以0次或者多次） => 功能验证  => 升级
     */

    // 新加
    @PostMapping("new")
    public Object newDag(@RequestBody ExecuteDagConfVO dagConf) {
        try {
            ExecuteDagConfVO added = dagConfigService.newDag(dagConf);
            logger.info("添加dag成功, req={}", dagConf);
            return ResponseMessage.success("success", added);
        } catch (Exception e) {
            logger.error("添加dag失败, req={}", dagConf, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    @PostMapping(value = "newDagWithYaml", consumes = "text/plain", produces = "application/json")
    public Object newWithYaml(@RequestBody String yamlConf) {
        try {
            YAMLMapper yamlMapper = YamlConfig.getYamlMapper();
            ExecuteDagConfVO req = yamlMapper.readValue(yamlConf, ExecuteDagConfVO.class);
            return ResponseMessage.success(dagConfigService.newDag(req));
        } catch (Exception e) {
            logger.error("添加dag失败, req={}", yamlConf, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    // 获取信息
    @PostMapping("get")
    public Object getDag(@RequestBody ExecuteDagConfVO dagConf) {
        try {
            ExecuteDagConfVO executeDagConf = dagConfigService.getDag(dagConf.getId());
            logger.info("下线dag成功, req={}", dagConf);
            return ResponseMessage.success("success", executeDagConf);
        } catch (Exception e) {
            logger.error("下线dag失败, req={}", dagConf, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    @PostMapping("listAllVersion")
    public Object listAllVersion(@RequestBody ExecuteDagConfVO dagConf) {
        try {
            List<ExecuteDagConfVO> executeDagConf = dagConfigService.listAllVersion(dagConf.getGroup(), dagConf.getBusinessName());
            logger.info("获取版本列表成功, req={}", dagConf);
            return ResponseMessage.success("success", executeDagConf);
        } catch (Exception e) {
            logger.error("获取版本列表失败, req={}", dagConf, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    // 新加版本
    @PostMapping("newVersion")
    public Object newDagVersion(@RequestBody ExecuteDagConfVO dagConf) {
        try {
            ExecuteDagConfVO added = dagConfigService.newDagVersion(dagConf);
            logger.info("添加dag成功, req={}", dagConf);
            return ResponseMessage.success("success", added);
        } catch (Exception e) {
            logger.error("添加dag失败, req={}", dagConf, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    // 指定版本(不能是online版本)更新
    @PostMapping("update")
    public Object updateDag(@RequestBody ExecuteDagConfVO dagConf) {
        try {
            Preconditions.checkArgument(dagConf.getId() != null, "id不能为空");
            ExecuteDagConfVO updated = dagConfigService.updateDag(dagConf);
            logger.info("升级dag成功, req={}", dagConf);
            return ResponseMessage.success("success", updated);
        } catch (Exception e) {
            logger.error("升级dag失败, req={}", dagConf, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    @PostMapping("upgrade")
    public Object upgradeDag(@RequestBody ExecuteDagConfVO dagConf) {
        try {
            Preconditions.checkArgument(dagConf.getId() != null, "id不能为空");
            ExecuteDagConfVO updated = dagConfigService.upgrade(dagConf);
            logger.info("升级dag成功, req={}", dagConf);
            return ResponseMessage.success("success", updated);
        } catch (Exception e) {
            logger.error("升级dag失败, req={}", dagConf, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }
    
    // 上线
    @PostMapping("online")
    public Object dagOnline(@RequestBody ExecuteDagConfVO dagConf) {
        try {
            ExecuteDagConfVO updated = dagConfigService.dagOnline(dagConf.getId());
            logger.info("上线dag成功, req={}", dagConf);
            return ResponseMessage.success("success", updated);
        } catch (Exception e) {
            logger.error("上线dag失败, req={}", dagConf, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    // 下线
    @PostMapping("offline")
    public Object dagOffline(@RequestBody ExecuteDagConfVO dagConf) {
        try {
            ExecuteDagConfVO updated = dagConfigService.dagOffline(dagConf.getId());
            logger.info("下线dag成功, req={}", dagConf);
            return ResponseMessage.success("success", updated);
        } catch (Exception e) {
            logger.error("下线dag失败, req={}", dagConf, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    @PostMapping("active")
    public Object dagActive(@RequestBody ExecuteDagConfVO dagConf) {
        try {
            ExecuteDagConfVO record = dagConfigService.activeConf(dagConf);
            return ResponseMessage.success("success", record);
        } catch (Exception e) {
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    @PostMapping("activeYaml")
    public ResponseEntity<String> activeYaml(@RequestBody ExecuteDagConfVO dagConf) {
        String response;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/x-yaml"));

        try {
            ExecuteDagConfVO record = dagConfigService.activeConf(dagConf);
            YAMLMapper yamlMapper = YamlConfig.getYamlMapper();
            response = yamlMapper.writeValueAsString(record);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

    @PostMapping(value = "dagUpdateYaml", consumes = "text/plain")
    public ResponseEntity<String> dagUpdateYaml(@RequestBody String yamlConf) {
        String response;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/x-yaml"));
        try {
            YAMLMapper yamlMapper = YamlConfig.getYamlMapper();

            ExecuteDagConfVO req = yamlMapper.readValue(yamlConf, ExecuteDagConfVO.class);
            if (req.getId() != null) {
                response = yamlMapper.writeValueAsString(dagConfigService.updateDag(req));
            } else {
                response = yamlMapper.writeValueAsString(dagConfigService.newDag(req));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

    @PostMapping("allActive")
    public Object dagAllActive() {
        try {
            List<ExecuteDagConfVO> record = dagConfigService.allActiveConf();
            return ResponseMessage.success("success", record);
        } catch (Exception e) {
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }
}
