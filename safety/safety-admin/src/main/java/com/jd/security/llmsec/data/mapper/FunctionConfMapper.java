package com.jd.security.llmsec.data.mapper;

import com.jd.security.llmsec.data.pojo.FunctionConf;
import com.jd.security.llmsec.data.pojo.FunctionConfExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface FunctionConfMapper {
    long countByExample(FunctionConfExample example);

    int deleteByExample(FunctionConfExample example);

    int deleteByPrimaryKey(Long id);

    int insert(FunctionConf row);

    int insertSelective(FunctionConf row);

    List<FunctionConf> selectByExampleWithBLOBsWithRowbounds(FunctionConfExample example, RowBounds rowBounds);

    List<FunctionConf> selectByExampleWithBLOBs(FunctionConfExample example);

    List<FunctionConf> selectByExampleWithRowbounds(FunctionConfExample example, RowBounds rowBounds);

    List<FunctionConf> selectByExample(FunctionConfExample example);

    FunctionConf selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") FunctionConf row, @Param("example") FunctionConfExample example);

    int updateByExampleWithBLOBs(@Param("row") FunctionConf row, @Param("example") FunctionConfExample example);

    int updateByExample(@Param("row") FunctionConf row, @Param("example") FunctionConfExample example);

    int updateByPrimaryKeySelective(FunctionConf row);

    int updateByPrimaryKeyWithBLOBs(FunctionConf row);

    int updateByPrimaryKey(FunctionConf row);
}