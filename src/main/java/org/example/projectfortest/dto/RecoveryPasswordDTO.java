package org.example.projectfortest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecoveryPasswordDTO {
    private String email;
    private String password;
}
