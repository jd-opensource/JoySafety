package com.jd.security.llmsec.contoller.data;

import com.google.common.base.Preconditions;
import com.jd.security.llmsec.core.ResponseMessage;
import com.jd.security.llmsec.data.pojo.SensitiveWords;
import com.jd.security.llmsec.pojo.data.SensitiveWordsVO;
import com.jd.security.llmsec.service.data.SensitiveWordsManageService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;



@RestController
@RequestMapping("/data/sensitive_words")
public class SensitiveWordsController {
    private Logger logger = LoggerFactory.getLogger(SensitiveWordsController.class);

    @Autowired
    private SensitiveWordsManageService sensitiveWordsManageService;

    @PostMapping("upsert")
    public Object upsert(@RequestBody SensitiveWordsVO vo) {
        try {
            SensitiveWords added = sensitiveWordsManageService.upsert(vo);
            logger.info("添加敏感词成功, req={}", vo);
            return ResponseMessage.success("success", added);
        } catch (Exception e) {
            logger.error("添加敏感词失败, req={}", vo, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    @PostMapping("updateWithId")
    public Object updateWithId(@RequestBody SensitiveWordsVO vo) {
        try {
            vo.setUpdateTime(new Date());
            SensitiveWords added = sensitiveWordsManageService.updateWithId(vo);
            logger.info("更新敏感词成功, req={}", vo);
            return ResponseMessage.success("success", added);
        } catch (Exception e) {
            logger.error("更新敏感词失败, req={}", vo, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    // 按Id查询
    @PostMapping("getById")
    public Object getById(@RequestBody SensitiveWordsVO vo) {
        try {
            Preconditions.checkNotNull(vo.getId(), "id is null");
            SensitiveWords word = sensitiveWordsManageService.getById(vo.getId());
            logger.info("查询敏感词成功, req={}", vo);
            return ResponseMessage.success("success", word);
        } catch (Exception e) {
            logger.error("查询敏感词失败, req={}", vo, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    @PostMapping("queryByWord")
    public Object queryByWord(@RequestBody SensitiveWordsVO vo) {
        try {
            Preconditions.checkArgument(StringUtils.isNotEmpty(vo.getWord()));
            List<SensitiveWords> words = sensitiveWordsManageService.queryByWord(vo.getWord(), vo);
            logger.info("查询敏感词成功, req={}", vo);
            return ResponseMessage.success("success", words);
        } catch (Exception e) {
            logger.error("查询敏感词失败, req={}", vo, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    @PostMapping("upgrade")
    public Object upgrade(@RequestBody SensitiveWordsVO vo) {
        try {
            Preconditions.checkNotNull(vo.getId(), "id is null");
            SensitiveWords word = sensitiveWordsManageService.upgrade(vo.getId());
            logger.info("切换敏感词成功, req={}", vo);
            return ResponseMessage.success("success", word);
        } catch (Exception e) {
            logger.error("切换上线敏感词失败, req={}", vo, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    // 上线
    @PostMapping("online")
    public Object online(@RequestBody SensitiveWordsVO vo) {
        try {
            Preconditions.checkNotNull(vo.getId(), "id is null");
            SensitiveWords word = sensitiveWordsManageService.online(vo.getId());
            logger.info("上线敏感词成功, req={}", vo);
            return ResponseMessage.success("success", word);
        } catch (Exception e) {
            logger.error("上线敏感词失败, req={}", vo, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }

    // 下线
    @PostMapping("offline")
    public Object offline(@RequestBody SensitiveWordsVO vo) {
        try {
            Preconditions.checkNotNull(vo.getId(), "id is null");
            SensitiveWords word = sensitiveWordsManageService.offline(vo.getId());
            logger.info("下线敏感词成功, req={}", vo);
            return ResponseMessage.success("success", word);
        } catch (Exception e) {
            logger.error("下线敏感词失败, req={}", vo, e);
            return ResponseMessage.fail(e.getMessage(), null);
        }
    }
}
