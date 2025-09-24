package com.jd.security.llmsec.contoller.business;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.jd.security.llmsec.config.YamlConfig;
import com.jd.security.llmsec.core.ResponseMessage;
import com.jd.security.llmsec.core.conf.ConfigReq;
import com.jd.security.llmsec.core.conf.ConfigResponse;
import com.jd.security.llmsec.pojo.business.BusinessInfoVO;
import com.jd.security.llmsec.pojo.business.ExecuteDagConfVO;
import com.jd.security.llmsec.pojo.business.FunctionConfVO;
import com.jd.security.llmsec.service.business.DagConfigService;
import com.jd.security.llmsec.service.business.DefenseConfigService;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;



@ControllerAdvice
@RequestMapping("/config/defense/api")
public class DefenseConfigApiController {
    private Logger logger = LoggerFactory.getLogger(DefenseConfigApiController.class);

    @Autowired
    private DefenseConfigService defenseConfigService;

    @PostMapping("conf")
    @ResponseBody
    public Object confs(@RequestBody ConfigReq req) {
        try {
            ConfigResponse configs = defenseConfigService.configs(req);
            logger.info("获取配置成功, req={}, response={}", JSON.toJSONString(req), JSON.toJSONString(configs));
            return ResponseMessage.success("success", configs);
        } catch (Exception e) {
            logger.error("添加dag失败, req={}", req, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    @Data
    public static class FuncBizReq {
        private String group = "default";
        private String functionName;
        private String businessName;
    }

    @PostMapping("func2Biz")
    @ResponseBody
    public Object func2Biz(@RequestBody FuncBizReq req) {
        try {
            List<BusinessInfoVO> bizs = defenseConfigService.func2Biz(req);
            logger.info("获取业务信息成功, req={}, response={}", JSON.toJSONString(req), JSON.toJSONString(bizs));
            return ResponseMessage.success("success", bizs);
        } catch (Exception e) {
            logger.error("添加业务信息失败, req={}", req, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    @PostMapping("biz2Func")
    @ResponseBody
    public Object biz2Func(@RequestBody FuncBizReq req) {
        try {
            List<FunctionConfVO> ret = defenseConfigService.biz2Func(req);
            logger.info("获取业务信息成功, req={}, response={}", JSON.toJSONString(req), JSON.toJSONString(ret));
            return ResponseMessage.success("success", ret);
        } catch (Exception e) {
            logger.error("添加业务信息失败, req={}", req, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }



    @PostMapping("confYaml")
    public ResponseEntity<String> confYaml(@RequestBody ConfigReq req) {
        String response;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/x-yaml"));
        try {
            YAMLMapper yamlMapper = YamlConfig.getYamlMapper();

            response = yamlMapper.writeValueAsString(((ResponseMessage)this.confs(req)).getData());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

    @Autowired
    @Qualifier("DagConfigServiceImpl")
    private DagConfigService dagConfigService;
    @PostMapping("dagYaml")
    public ResponseEntity<String> dagYaml(@RequestBody ExecuteDagConfVO dagConf) {
        String response;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/x-yaml"));
        try {
            YAMLMapper yamlMapper = YamlConfig.getYamlMapper();
            if (dagConf.getId() != null) {
                response = yamlMapper.writeValueAsString(dagConfigService.getDag(dagConf.getId()));
            } else {
                response = yamlMapper.writeValueAsString(dagConfigService.activeConf(dagConf));
            }
        } catch (JsonProcessingException e) {
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
}
