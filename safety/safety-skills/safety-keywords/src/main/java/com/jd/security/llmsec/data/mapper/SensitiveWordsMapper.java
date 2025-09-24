package com.jd.security.llmsec.data.mapper;

import com.jd.security.llmsec.data.pojo.SensitiveWords;
import com.jd.security.llmsec.data.pojo.SensitiveWordsExample;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface SensitiveWordsMapper {
    long countByExample(SensitiveWordsExample example);

    int deleteByExample(SensitiveWordsExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SensitiveWords row);

    int insertSelective(SensitiveWords row);

    List<SensitiveWords> selectByExampleWithBLOBsWithRowbounds(SensitiveWordsExample example, RowBounds rowBounds);

    List<SensitiveWords> selectByExampleWithBLOBs(SensitiveWordsExample example);

    List<SensitiveWords> selectByExampleWithRowbounds(SensitiveWordsExample example, RowBounds rowBounds);

    List<SensitiveWords> selectByExample(SensitiveWordsExample example);

    SensitiveWords selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") SensitiveWords row, @Param("example") SensitiveWordsExample example);

    int updateByExampleWithBLOBs(@Param("row") SensitiveWords row, @Param("example") SensitiveWordsExample example);

    int updateByExample(@Param("row") SensitiveWords row, @Param("example") SensitiveWordsExample example);

    int updateByPrimaryKeySelective(SensitiveWords row);

    int updateByPrimaryKeyWithBLOBs(SensitiveWords row);

    int updateByPrimaryKey(SensitiveWords row);
}