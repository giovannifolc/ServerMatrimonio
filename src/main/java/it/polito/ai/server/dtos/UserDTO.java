package it.polito.ai.server.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@AllArgsConstructor
@Data
public class UserDTO {
    @NotEmpty
    private String id;
    @NotEmpty
    private String role;
    @Email
    private String email;
    @NotEmpty
    private String password;
}
