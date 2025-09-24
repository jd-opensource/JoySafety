package com.jd.security.llmsec.service.business;

import com.jd.security.llmsec.pojo.business.BusinessInfoVO;

import java.util.List;



public interface BusinessConfigService {
    BusinessInfoVO newBusiness(BusinessInfoVO businessConf);

    BusinessInfoVO getBusiness(Long id);

    List<BusinessInfoVO> listAllVersions(BusinessInfoVO req);

    BusinessInfoVO newBusinessVersion(BusinessInfoVO businessConf);

    BusinessInfoVO updateBusiness(BusinessInfoVO businessConf);

    BusinessInfoVO upgradeBusiness(BusinessInfoVO newVersion);

    BusinessInfoVO businessOnline(Long id);

    BusinessInfoVO businessOffline(Long id);

    BusinessInfoVO activeConf(String group, String businessName);
    List<BusinessInfoVO> allActiveConf();
}
