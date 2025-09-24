package com.jd.security.llmsec.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

public class YamlConfig {
    public static YAMLMapper getYamlMapper() {
        YAMLFactory yamlFactory = new YAMLFactory()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                .enable(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE);
        YAMLMapper yamlMapper = new YAMLMapper(yamlFactory);
        yamlMapper.configure(YAMLGenerator.Feature.USE_PLATFORM_LINE_BREAKS, true);
        yamlMapper.configure(YAMLGenerator.Feature.SPLIT_LINES, true);

        yamlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        yamlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return yamlMapper;
    }
}
