package net.umpay.mailbill.hql.model;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * 统一定义id的entity基类.
 * 
 * 基类统一定义id的属性名称、数据类型、列名映射及生成策略. 子类可重载getId()函数重定义id的列名映射和生成策略.
 * 
 * liuyt
 */
// JPA Entity基类的标识
@MappedSuperclass
public abstract class IdEntity implements Serializable {
    

	private static final long serialVersionUID = -6587379183315653617L;
	
	private Long id;
    @Id
    @GeneratedValue(generator = "paymentableGenerator")
    @GenericGenerator(name = "paymentableGenerator", strategy = "sequence", parameters = { @Parameter(name = "sequence", value = "mail_bill_sequence") })
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}