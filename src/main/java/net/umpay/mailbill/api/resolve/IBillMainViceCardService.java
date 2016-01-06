package net.umpay.mailbill.api.resolve;

import java.util.List;

import net.umpay.mailbill.hql.model.BillMainViceCardEntity;

/**
 * 主副卡关系的接口
 * @author admin
 */
public interface IBillMainViceCardService {

	/**
	 * 在主副卡关系内查询信息
	 * @param bankId				银行id
	 * @param accountId				账户信息
	 * @param infoSourceEmail		邮箱地址
	 * @param mainCardOfFour		主卡卡号
	 * @return List<BillMainViceCardEntity>
	 */
	public List<BillMainViceCardEntity> find(int bankId, long accountId, String infoSourceEmail, String mainCardOfFour);
	/**
	 * 在主副卡关系内添加信息
	 * @param bankId				银行id
	 * @param accountId				账户信息
	 * @param infoSourceEmail		邮箱地址
	 * @param mainCardOfFour		主卡卡号
	 * @param viceCardOfFour		副卡卡号
	 */
	public void save(int bankId, Long accountId, String infoSourceEmail, String mainCardOfFour, String viceCardOfFour);
	
	/**
	 * 在主副卡关系内更新信息
	 * @param billMainViceCardEntity
	 */
	public void update(BillMainViceCardEntity billMainViceCardEntity);
}
