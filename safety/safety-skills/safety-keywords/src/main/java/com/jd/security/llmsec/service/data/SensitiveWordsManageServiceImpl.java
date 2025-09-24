package com.jd.security.llmsec.service.data;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jd.security.llmsec.core.rpc.KeywordApiService.KVRequest;
import com.jd.security.llmsec.data.mapper.SensitiveWordsMapper;
import com.jd.security.llmsec.data.pojo.SensitiveWords;
import com.jd.security.llmsec.data.pojo.SensitiveWordsExample;
import com.jd.security.llmsec.pojo.MatchType;
import com.jd.security.llmsec.utils.WordsUtils;
import org.ahocorasick.trie.Emit;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SensitiveWordsManageServiceImpl implements SensitiveWordsManageService {
    private Logger logger = LoggerFactory.getLogger(SensitiveWordsManageServiceImpl.class);
    @Autowired
    private SensitiveWordsMapper sensitiveWordsMapper;

    private final int MAX_PROCESS_MILLISECONDS = 150;

    private Map<String, List<MatchType.SensitiveWordsVO>> wordsMap = new HashMap<>();

    private ExecutorService pool;
    private SensitiveWordDetector containDetector = new SensitiveWordDetector(new HashMap<>());

    private Map<String, Set<Long>> regexInvertMap;
    private Map<Long, MatchType.SensitiveWordsVO> id2WordsMap = new HashMap<>();
    private SensitiveWordDetector regexDetector = null;

    @PostConstruct
    public void loadSensitiveWords() {
        updateAll();
        this.pool = new ThreadPoolExecutor(200, 200,
                10, TimeUnit.MINUTES, new LinkedBlockingQueue<>(10), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Override
    public MatchType.SensitiveWordsVO check(KVRequest request) throws ExecutionException, InterruptedException {
        String content = request.getContent();
        String bizName = request.getBusinessName();
        MatchType.SensitiveWordsVO ret = parallelDetection(content, bizName);
        return ret;
    }

    public MatchType.SensitiveWordsVO parallelDetection(String content, String bizName) throws ExecutionException, InterruptedException {
        long start = System.currentTimeMillis();
        Future<MatchType.SensitiveWordsVO> regexTask = pool.submit(() -> doRegexTask(content, bizName));
        Future<MatchType.SensitiveWordsVO> containTask = pool.submit(() -> doContainTask(content, bizName));
        MatchType.SensitiveWordsVO ret1;
        int waitMillis = MAX_PROCESS_MILLISECONDS;
        try {
            ret1 = containTask.get(waitMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            logger.warn("敏感词匹配超时，biz={}, query={}，cost={}ms", bizName, content, System.currentTimeMillis() - start);
            return null;
        }

        if (ret1 != null && Objects.equals("black", ret1.getHandleStrategy())) {
            regexTask.cancel(true);
            return ret1;
        } else {
            try {
                MatchType.SensitiveWordsVO ret2 = regexTask.get(waitMillis - (System.currentTimeMillis() - start), TimeUnit.MILLISECONDS);
                if (ret2 == null) {
                    return ret1;
                } else {
                    if (Objects.equals("black", ret2.getHandleStrategy())) {
                        return ret2;
                    } else {
                        return ret1 != null ? ret1 : ret2;
                    }
                }
            } catch (TimeoutException e) {
                logger.warn("正则匹配超时，biz={}, query={}，cost={}ms", bizName, content, System.currentTimeMillis() - start);
                return null;
            }
        }
    }

    private MatchType.SensitiveWordsVO doContainTask(String content, String bizName) {
        long startTime = System.currentTimeMillis();
        List<Emit> detectedWords = containDetector.detectSensitiveWords(content);
        if (!detectedWords.isEmpty()) {
            List<String> words = detectedWords.stream().map(x -> x.getKeyword()).collect(Collectors.toList());
            List<MatchType.SensitiveWordsVO> rets = Lists.newArrayList();
            for (String hitWord : words) {
                List<MatchType.SensitiveWordsVO> hitWordVOs = wordsMap.get(hitWord);
                if (CollectionUtils.isEmpty(hitWordVOs)) {
                    continue;
                }
                for (MatchType.SensitiveWordsVO one : hitWordVOs) {
                    if (isSceneMatch(bizName, one.getBusinessScene())) {
                        if (Objects.equals(MatchType.equal.name(), one.getMatchType())) {
                            if (!Objects.equals(content, one.getWord())) {
                                continue;
                            }
                        }
                        rets.add(one);
                    }
                }
            }

            if (CollectionUtils.isNotEmpty(rets)) {
                rets.sort(WordsUtils.WORDS_Comparator);
                MatchType.SensitiveWordsVO hitWordVO = rets.get(0);
                if (Objects.equals(MatchType.exclude.name(), hitWordVO.getHandleStrategy())) {
                    logger.info("业务忽略敏感词, bizName={}, content={}，hitword={}, cost={}ms", bizName, content, JSON.toJSONString(hitWordVO), System.currentTimeMillis() - startTime);
                    return null;
                } else {
                    logger.info("命中敏感词, bizName={}, content={}，hitword={}, cost={}ms", bizName, content, JSON.toJSONString(hitWordVO), System.currentTimeMillis() - startTime);
                    return hitWordVO;
                }
            }
        }
        logger.debug("未命中敏感词, bizName={}, content={}, cost={}ms", bizName, content, System.currentTimeMillis() - startTime);
        return null;
    }

    private MatchType.SensitiveWordsVO doRegexTask(String content, String bizName) {
        long startTime = System.currentTimeMillis();
        List<Emit> emits = regexDetector.detectSensitiveWords(content);
        if (CollectionUtils.isEmpty(emits)) {
            logger.debug("正则trie树中未匹配，biz={}, content={}", bizName, content);
            return null;
        }
        Set<String> matchedKeywords = emits.stream().map(Emit::getKeyword).collect(Collectors.toSet());
        List<MatchType.SensitiveWordsVO> matchedWordVos = queryPattern(matchedKeywords, bizName);
        if (CollectionUtils.isEmpty(matchedWordVos)) {
            logger.debug("倒排doc未匹配，biz={}, content={}", bizName, content);
            return null;
        }

        matchedWordVos.sort(WordsUtils.WORDS_Comparator);
        for (MatchType.SensitiveWordsVO sensitiveWordsVO : matchedWordVos) {
            if (sensitiveWordsVO.getPattern().matcher(content).find()) {
                logger.info("命中正则, bizName={}, content={}，hitword={}, cost={}ms", bizName, content, JSON.toJSONString(sensitiveWordsVO), System.currentTimeMillis() - startTime);
                return sensitiveWordsVO;
            }
        }

        logger.debug("所有正则未匹配，biz={}, content={}, cost={}ms", bizName, content, System.currentTimeMillis() - startTime);
        return null;
    }

    private List<MatchType.SensitiveWordsVO> queryPattern(Set<String> matchedKeywords, String bizName) {
        Set<Long> docIds = Sets.newHashSet();
        for (String keyword : matchedKeywords) {
            Set<Long> temp = regexInvertMap.get(keyword);
            if (CollectionUtils.isNotEmpty(temp)) {
                docIds.addAll(temp);
            }
        }
        if (docIds.isEmpty()) {
            return null;
        }

        return docIds.stream().map(x -> id2WordsMap.get(x))
                .filter(x -> isSceneMatch(bizName,x.getBusinessScene()))
                .collect(Collectors.toList());
    }

    private Boolean isSceneMatch(String bizName, String businessScene) {
        Boolean result = Boolean.FALSE;
        if (businessScene.equals("all") || (businessScene != null && businessScene.trim().isEmpty())) {
            result = Boolean.TRUE;
        } else {
            String[] bizNames = businessScene.split(",");
            if (bizNames.length > 0 && Arrays.asList(bizNames).contains(bizName)) {
                result = Boolean.TRUE;
            }
        }
        return result;
    }

    private static final long refreshIntervalMilliseconds = 60 * 1000;
    @Scheduled(fixedRate = refreshIntervalMilliseconds)
    public void checkForUpdates() {
        updateAll();
    }

    private void updateAll() {
        updateContains();
        updateRegex();
    }

    private long regexLastRefreshTime = -1;
    private void updateRegex() {
        /*
        1. 索引构建：
            1.1 对正则进行分词，对分词进行去重，并进行倒排；
            1.2 将正则中的非特殊字符放入trie树，用于对query进行匹配；
        2. 查询：
            2.1 使用trie树对query进行分词；
            2.2 分词结果查询倒排表，并通过倒排表查询到具体正则；
            2.3 使用正则对query进行匹配（考虑匹配到的正则过多的情况/事先规范正则）；
         */

        if (regexLastRefreshTime > 0) {
            SensitiveWordsExample q = new SensitiveWordsExample();
            q.createCriteria().andMatchTypeEqualTo(MatchType.regex.name());
            q.setOrderByClause(" update_time desc  limit 1 ");
            List<SensitiveWords> sensitiveWords = sensitiveWordsMapper.selectByExampleWithBLOBs(q);
            if (CollectionUtils.isEmpty(sensitiveWords)) {
                logger.warn("获取regex类敏感词失败");
                return;
            }

            long latestDataModifyTime = sensitiveWords.get(0).getUpdateTime().getTime();
            if (latestDataModifyTime < regexLastRefreshTime) {
                logger.info("自上次数据刷新后regex数据没有更新过，不用重新刷新数据");
                return;
            }
            logger.info("regex数据有更新，重新刷新数据");
        }

        List<MatchType.SensitiveWordsVO> newRegexPatterns = getWordByMatchType(Collections.singletonList(MatchType.regex.name()));

        Map<Long, MatchType.SensitiveWordsVO> id2WordsMap = new HashMap<>();

        Map<String, Set<Long>> invertMap = new HashMap<>();
        Set<String> keywords = new HashSet<>();
        for (MatchType.SensitiveWordsVO wordsVO : newRegexPatterns) {
            id2WordsMap.put(wordsVO.getId(), wordsVO);

            try {
                Pattern pattern = Pattern.compile(wordsVO.getWord());
                wordsVO.setPattern(pattern);
            } catch (Exception e) {
                logger.error("构建pattern失败，word={}", JSON.toJSONString(wordsVO), e);
                continue;
            }

            Set<String> words = regexTokenize(wordsVO.getWord());
            if (CollectionUtils.isEmpty(words)) {
                logger.warn("正则表达式分词结果为空，word={}", JSON.toJSONString(wordsVO));
                continue;
            }
            for (String word : words) {
                Set<Long> docs = invertMap.get(word);
                if (docs == null) {
                    docs = Sets.newHashSet();
                    invertMap.put(word, docs);
                }
                docs.add(wordsVO.getId());
            }
            keywords.addAll(words);
        }

        this.regexDetector = new SensitiveWordDetector(keywords);
        this.id2WordsMap = id2WordsMap;
        this.regexInvertMap = invertMap;

        regexLastRefreshTime = System.currentTimeMillis();
    }

    private final String splitRgex = "(\\(|\\.|\\)|&|(\\{\\d+\\})|(\\{\\d+,\\})|(\\{\\d+,\\d+\\})|(\\{,\\d+\\})|\\?<=|\\?<!|\\?=|\\?!|\\*|\\?|\\+|\\[|\\]|\\(|\\)|\\{|\\}|\\|)+";
    private Set<String> regexTokenize(String word) {
        List<String> split = Splitter.onPattern(splitRgex).omitEmptyStrings().trimResults().splitToList(word);
        return new HashSet<>(split);
    }

    private long containsLastRefreshTime = -1;
    private void updateContains() {
        List<String> types = Arrays.asList(MatchType.contain.name(), MatchType.equal.name());
        if (containsLastRefreshTime > 0) {
            SensitiveWordsExample q = new SensitiveWordsExample();
            q.createCriteria().andMatchTypeIn(types);
            q.setOrderByClause(" update_time desc limit 1 ");
            List<SensitiveWords> sensitiveWords = sensitiveWordsMapper.selectByExampleWithBLOBs(q);
            if (CollectionUtils.isEmpty(sensitiveWords)) {
                logger.warn("获取contain类敏感词失败");
                return;
            }

            long latestDataModifyTime = sensitiveWords.get(0).getUpdateTime().getTime();
            if (latestDataModifyTime < containsLastRefreshTime) {
                logger.info("自上次数据刷新后contain数据没有更新过，不用重新刷新数据");
                return;
            }
            logger.info("contain数据有更新，重新刷新数据");
        }

        List<MatchType.SensitiveWordsVO> newSensitiveWords = getWordByMatchType(types);
        if (CollectionUtils.isEmpty(newSensitiveWords)) {
            logger.warn("获取contain类敏感词失败");
            return;
        }

        Map<String, List<MatchType.SensitiveWordsVO>> newWordsMap = new HashMap<>();


        for (MatchType.SensitiveWordsVO word : newSensitiveWords) {
            List<MatchType.SensitiveWordsVO> list = newWordsMap.get(word.getWord());
            if (list == null) {
                list = Lists.newArrayList();
                newWordsMap.put(word.getWord(), list);
            }
            list.add(word);
        }

        if (newWordsMap.size() != 0) {
            //构建检测器
            containDetector = new SensitiveWordDetector(newWordsMap);
            this.wordsMap = newWordsMap;
        }
        containsLastRefreshTime = System.currentTimeMillis();
    }

    private List<MatchType.SensitiveWordsVO> getWordByMatchType(List<String> matchTypes) {
        logger.info("分类别获取数据开始，matchType={}", matchTypes);
        long startTime = System.currentTimeMillis();

        long startId = 1;
        List<MatchType.SensitiveWordsVO> ret = Lists.newArrayList();
        while (true) {
            List<SensitiveWords> sensitiveWords = doQuery(matchTypes, startId);
            if (CollectionUtils.isEmpty(sensitiveWords)) {
                break;
            } else {
                ret.addAll(convert(sensitiveWords));
                startId = ret.get(ret.size() - 1).getId() + 1;
            }
        }
        logger.info("分类别获取数据结束，matchType={}，count={}, cost={}ms", matchTypes, ret.size(), System.currentTimeMillis() - startTime);
        return ret;
    }

    private List<MatchType.SensitiveWordsVO> convert(List<SensitiveWords> sensitiveWords) {
        List<MatchType.SensitiveWordsVO> ret = Lists.newArrayList();
        for (SensitiveWords sensitiveWord : sensitiveWords) {
            MatchType.SensitiveWordsVO sensitiveWordVO = new MatchType.SensitiveWordsVO();
            BeanUtils.copyProperties(sensitiveWord, sensitiveWordVO);
            ret.add(sensitiveWordVO);
        }
        return ret;
    }

    private List<SensitiveWords> doQuery(List<String> matchTypes, long startId) {
        SensitiveWordsExample q = new SensitiveWordsExample();
        q.createCriteria().andIdGreaterThanOrEqualTo(startId)
                .andStatusEqualTo("online")
                .andMatchTypeIn(matchTypes);
        q.setOrderByClause(" id asc limit 5000 ");
        return sensitiveWordsMapper.selectByExampleWithBLOBs(q);
    }
}
