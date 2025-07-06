package org.crystallen.lc.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Users implements Serializable {
    private Long id;
    private String username;
    private String email;
    private String password;
    private LocalDateTime createdAt;
}