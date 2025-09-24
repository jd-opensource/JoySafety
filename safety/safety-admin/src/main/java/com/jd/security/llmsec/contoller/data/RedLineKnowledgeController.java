package com.jd.security.llmsec.contoller.data;

import com.google.common.base.Preconditions;
import com.jd.security.llmsec.core.ResponseMessage;
import com.jd.security.llmsec.data.pojo.RedLineKnowledgeWithBLOBs;
import com.jd.security.llmsec.pojo.data.RedLineKnowledgeVO;
import com.jd.security.llmsec.service.data.RedLineKnowledgeManageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;



@RestController
@RequestMapping("/data/redline_knowledge")
public class RedLineKnowledgeController {
    private Logger logger = LoggerFactory.getLogger(RedLineKnowledgeController.class);

    @Autowired
    private RedLineKnowledgeManageService redLineKnowledgeManageService;

    // 新加/更新
    @PostMapping("upsert")
    public Object upsert(@RequestBody RedLineKnowledgeVO vo) {
        try {
            RedLineKnowledgeWithBLOBs added = redLineKnowledgeManageService.upsert(vo);
            logger.info("添加红线知识成功, req={}", vo);
            return ResponseMessage.success("success", added);
        } catch (Exception e) {
            logger.error("添加红线知识失败, req={}", vo, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    // 按Id查询
    @PostMapping("getById")
    public Object getById(@RequestBody RedLineKnowledgeVO vo) {
        try {
            Preconditions.checkNotNull(vo.getId(), "id is null");
            RedLineKnowledgeWithBLOBs word = redLineKnowledgeManageService.getById(vo.getId());
            logger.info("查询红线知识成功, req={}", vo);
            return ResponseMessage.success("success", word);
        } catch (Exception e) {
            logger.error("查询红线知识失败, req={}", vo, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    @PostMapping("getByWord")
    public Object getByWord(@RequestBody RedLineKnowledgeVO vo) {
        try {
            Map<String,RedLineKnowledgeWithBLOBs> word = redLineKnowledgeManageService.getByWord(vo);
            logger.info("查询红线知识成功, req={}", vo);
            return ResponseMessage.success("success", word);
        } catch (Exception e) {
            logger.error("查询红线知识失败, req={}", vo, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    // 新加/更新
    @PostMapping("update")
    public Object update(@RequestBody RedLineKnowledgeVO vo) {
        try {
            vo.setUpdateTime(new Date());
            RedLineKnowledgeWithBLOBs added = redLineKnowledgeManageService.update(vo);
            logger.info("更新红线知识成功, req={}", vo);
            return ResponseMessage.success("success", added);
        } catch (Exception e) {
            logger.error("更新红线知识失败, req={}", vo, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    // 上线
    @PostMapping("online")
    public Object online(@RequestBody RedLineKnowledgeVO vo) {
        try {
            Preconditions.checkNotNull(vo.getId(), "id is null");
            RedLineKnowledgeWithBLOBs word = redLineKnowledgeManageService.online(vo.getId());
            logger.info("上线红线知识成功, req={}", vo);
            return ResponseMessage.success("success", word);
        } catch (Exception e) {
            logger.error("上线红线知识失败, req={}", vo, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    // 下线
    @PostMapping("offline")
    public Object offline(@RequestBody RedLineKnowledgeVO vo) {
        try {
            Preconditions.checkNotNull(vo.getId(), "id is null");
            RedLineKnowledgeWithBLOBs word = redLineKnowledgeManageService.offline(vo.getId());
            logger.info("下线红线知识成功, req={}", vo);
            return ResponseMessage.success("success", word);
        } catch (Exception e) {
            logger.error("下线红线知识失败, req={}", vo, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }
}
