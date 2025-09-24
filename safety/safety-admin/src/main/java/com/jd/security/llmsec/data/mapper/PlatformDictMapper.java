package com.jd.security.llmsec.data.mapper;

import com.jd.security.llmsec.data.pojo.PlatformDict;
import com.jd.security.llmsec.data.pojo.PlatformDictExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface PlatformDictMapper {
    long countByExample(PlatformDictExample example);

    int deleteByExample(PlatformDictExample example);

    int deleteByPrimaryKey(Long id);

    int insert(PlatformDict row);

    int insertSelective(PlatformDict row);

    List<PlatformDict> selectByExampleWithBLOBsWithRowbounds(PlatformDictExample example, RowBounds rowBounds);

    List<PlatformDict> selectByExampleWithBLOBs(PlatformDictExample example);

    List<PlatformDict> selectByExampleWithRowbounds(PlatformDictExample example, RowBounds rowBounds);

    List<PlatformDict> selectByExample(PlatformDictExample example);

    PlatformDict selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") PlatformDict row, @Param("example") PlatformDictExample example);

    int updateByExampleWithBLOBs(@Param("row") PlatformDict row, @Param("example") PlatformDictExample example);

    int updateByExample(@Param("row") PlatformDict row, @Param("example") PlatformDictExample example);

    int updateByPrimaryKeySelective(PlatformDict row);

    int updateByPrimaryKeyWithBLOBs(PlatformDict row);

    int updateByPrimaryKey(PlatformDict row);
}