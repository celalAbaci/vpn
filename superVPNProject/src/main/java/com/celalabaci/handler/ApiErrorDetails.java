package com.celalabaci.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorDetails<E> {
    private String path;
    private Date createTime;
    // Güvenlik nedeniyle hostName alanı kaldırıldı.
    // private String hostName;
    private E message;
}
