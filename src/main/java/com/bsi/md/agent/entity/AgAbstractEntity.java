package com.bsi.md.agent.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * 实体类父类
 */
@Data
@MappedSuperclass
public class AgAbstractEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "fwId")
    @GenericGenerator(name = "fwId", strategy = "com.bsi.framework.core.idgenerator.FwIdGenerator")
    private Integer id;
    private Boolean enable;
}
