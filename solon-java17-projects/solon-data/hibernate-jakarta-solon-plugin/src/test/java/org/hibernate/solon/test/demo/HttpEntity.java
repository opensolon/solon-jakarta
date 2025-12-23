package org.hibernate.solon.test.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "http_entity")
public class HttpEntity extends BaseTime {
    @Id
    private String id;
    @Column(name = "description")
    private String description;
    @Column(name = "type")
    private String type;
}