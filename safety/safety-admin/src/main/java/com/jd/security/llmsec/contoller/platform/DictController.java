package com.jd.security.llmsec.contoller.platform;

import com.alibaba.fastjson2.JSON;
import com.jd.security.llmsec.core.ResponseMessage;
import com.jd.security.llmsec.pojo.platform.dict.DictVo;
import com.jd.security.llmsec.service.platform.PlatformDictService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;




@CrossOrigin(origins = "*",maxAge = 3600,allowCredentials = "true")
@RestController
@RequestMapping("/platform/dict")
public class DictController {
    private Logger logger = LoggerFactory.getLogger(DictController.class);

    @Autowired
    private PlatformDictService platformDictService;

    // 更新（没有时新建）
    @PostMapping("upgrade")
    public Object upgrade(@RequestBody DictVo req) {
        try {
            DictVo ret = platformDictService.upgrade(req);
            logger.info("字典更新成功,req={}, resp={}", JSON.toJSONString(req), JSON.toJSONString(ret));
            return ResponseMessage.success("success", ret);
        } catch (Exception e) {
            logger.error("字典更新失败,req={}",  JSON.toJSONString(req), e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    @PostMapping("get")
    public Object get(@RequestBody DictVo req) {
        try {
            DictVo ret = platformDictService.get(req.getKey());
            logger.info("查询字典成功,req={}, resp={}", JSON.toJSONString(req), JSON.toJSONString(ret));
            return ResponseMessage.success("success", ret);
        } catch (Exception e) {
            logger.error("查询字典失败,req={}",  JSON.toJSONString(req), e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    @PostMapping("list")
    public Object query(@RequestBody DictVo req) {
        try {
            List<DictVo> ret = platformDictService.list(req);
            logger.info("查询字典成功,req={}, resp={}", JSON.toJSONString(req), JSON.toJSONString(ret));
            return ResponseMessage.success("success", ret);
        } catch (Exception e) {
            logger.error("查询字典失败,req={}",  JSON.toJSONString(req), e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    @PostMapping("online")
    public Object online(@RequestBody DictVo req) {
        try {
            DictVo ret = platformDictService.online(req);
            logger.info("字典值上线成功,req={}, resp={}", JSON.toJSONString(req), JSON.toJSONString(ret));
            return ResponseMessage.success("success", ret);
        } catch (Exception e) {
            logger.error("字典值上线失败,req={}",  JSON.toJSONString(req), e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    @PostMapping("offline")
    public Object offline(@RequestBody DictVo req) {
        try {
            DictVo ret = platformDictService.offline(req);
            logger.info("字典值下线成功,req={}, resp={}", JSON.toJSONString(req), JSON.toJSONString(ret));
            return ResponseMessage.success("success", ret);
        } catch (Exception e) {
            logger.error("字典值下线失败,req={}",  JSON.toJSONString(req), e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

}
