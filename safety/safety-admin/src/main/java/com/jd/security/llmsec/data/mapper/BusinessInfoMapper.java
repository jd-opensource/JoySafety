package com.jd.security.llmsec.data.mapper;

import com.jd.security.llmsec.data.pojo.BusinessInfo;
import com.jd.security.llmsec.data.pojo.BusinessInfoExample;
import com.jd.security.llmsec.data.pojo.BusinessInfoWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface BusinessInfoMapper {
    long countByExample(BusinessInfoExample example);

    int deleteByExample(BusinessInfoExample example);

    int deleteByPrimaryKey(Long id);

    int insert(BusinessInfoWithBLOBs row);

    int insertSelective(BusinessInfoWithBLOBs row);

    List<BusinessInfoWithBLOBs> selectByExampleWithBLOBsWithRowbounds(BusinessInfoExample example, RowBounds rowBounds);

    List<BusinessInfoWithBLOBs> selectByExampleWithBLOBs(BusinessInfoExample example);

    List<BusinessInfo> selectByExampleWithRowbounds(BusinessInfoExample example, RowBounds rowBounds);

    List<BusinessInfo> selectByExample(BusinessInfoExample example);

    BusinessInfoWithBLOBs selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") BusinessInfoWithBLOBs row, @Param("example") BusinessInfoExample example);

    int updateByExampleWithBLOBs(@Param("row") BusinessInfoWithBLOBs row, @Param("example") BusinessInfoExample example);

    int updateByExample(@Param("row") BusinessInfo row, @Param("example") BusinessInfoExample example);

    int updateByPrimaryKeySelective(BusinessInfoWithBLOBs row);

    int updateByPrimaryKeyWithBLOBs(BusinessInfoWithBLOBs row);

    int updateByPrimaryKey(BusinessInfo row);
}