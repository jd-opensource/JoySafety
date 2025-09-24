package com.jd.security.llmsec.service.data;

import com.jd.security.llmsec.pojo.MatchType;
import org.ahocorasick.trie.Trie;
import org.ahocorasick.trie.Emit;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SensitiveWordDetector {
    private Trie trie;

    public SensitiveWordDetector(Map<String, List<MatchType.SensitiveWordsVO>> sensitiveWordsMap) {
        Trie.TrieBuilder builder = Trie.builder();
        for (String word : sensitiveWordsMap.keySet()) {
            builder.addKeyword(word);
        }
        trie = builder.build();
    }

    public SensitiveWordDetector(Collection<String> keywords) {
        Trie.TrieBuilder builder = Trie.builder();
        builder.addKeywords(keywords);
        trie = builder.build();
    }

    public List<Emit> detectSensitiveWords(String text) {
        return (List<Emit>) trie.parseText(text);
    }
}
