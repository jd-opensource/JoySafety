package com.jd.security.llmsec.data.mapper;

import com.jd.security.llmsec.data.pojo.ExecuteDagConf;
import com.jd.security.llmsec.data.pojo.ExecuteDagConfExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface ExecuteDagConfMapper {
    long countByExample(ExecuteDagConfExample example);

    int deleteByExample(ExecuteDagConfExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ExecuteDagConf row);

    int insertSelective(ExecuteDagConf row);

    List<ExecuteDagConf> selectByExampleWithBLOBsWithRowbounds(ExecuteDagConfExample example, RowBounds rowBounds);

    List<ExecuteDagConf> selectByExampleWithBLOBs(ExecuteDagConfExample example);

    List<ExecuteDagConf> selectByExampleWithRowbounds(ExecuteDagConfExample example, RowBounds rowBounds);

    List<ExecuteDagConf> selectByExample(ExecuteDagConfExample example);

    ExecuteDagConf selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") ExecuteDagConf row, @Param("example") ExecuteDagConfExample example);

    int updateByExampleWithBLOBs(@Param("row") ExecuteDagConf row, @Param("example") ExecuteDagConfExample example);

    int updateByExample(@Param("row") ExecuteDagConf row, @Param("example") ExecuteDagConfExample example);

    int updateByPrimaryKeySelective(ExecuteDagConf row);

    int updateByPrimaryKeyWithBLOBs(ExecuteDagConf row);

    int updateByPrimaryKey(ExecuteDagConf row);
}