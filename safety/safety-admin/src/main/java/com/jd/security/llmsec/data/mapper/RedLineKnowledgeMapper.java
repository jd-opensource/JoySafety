package com.jd.security.llmsec.data.mapper;

import com.jd.security.llmsec.data.pojo.RedLineKnowledge;
import com.jd.security.llmsec.data.pojo.RedLineKnowledgeExample;
import com.jd.security.llmsec.data.pojo.RedLineKnowledgeWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface RedLineKnowledgeMapper {
    long countByExample(RedLineKnowledgeExample example);

    int deleteByExample(RedLineKnowledgeExample example);

    int deleteByPrimaryKey(Long id);

    int insert(RedLineKnowledgeWithBLOBs row);

    int insertSelective(RedLineKnowledgeWithBLOBs row);

    List<RedLineKnowledgeWithBLOBs> selectByExampleWithBLOBsWithRowbounds(RedLineKnowledgeExample example, RowBounds rowBounds);

    List<RedLineKnowledgeWithBLOBs> selectByExampleWithBLOBs(RedLineKnowledgeExample example);

    List<RedLineKnowledge> selectByExampleWithRowbounds(RedLineKnowledgeExample example, RowBounds rowBounds);

    List<RedLineKnowledge> selectByExample(RedLineKnowledgeExample example);

    RedLineKnowledgeWithBLOBs selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") RedLineKnowledgeWithBLOBs row, @Param("example") RedLineKnowledgeExample example);

    int updateByExampleWithBLOBs(@Param("row") RedLineKnowledgeWithBLOBs row, @Param("example") RedLineKnowledgeExample example);

    int updateByExample(@Param("row") RedLineKnowledge row, @Param("example") RedLineKnowledgeExample example);

    int updateByPrimaryKeySelective(RedLineKnowledgeWithBLOBs row);

    int updateByPrimaryKeyWithBLOBs(RedLineKnowledgeWithBLOBs row);

    int updateByPrimaryKey(RedLineKnowledge row);
}