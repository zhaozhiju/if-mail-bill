package net.umpay.mailbill.api.resolve;

import net.umpay.mailbill.hql.model.LeadInfoEntity;
/**
 * 邮箱引导统计信息
 * @author admin
 */
public interface ILeadInfoService {

	public void save(LeadInfoEntity entity);
}
