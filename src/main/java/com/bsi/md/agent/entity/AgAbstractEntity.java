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
    private String id;
    private Boolean enable;
}
