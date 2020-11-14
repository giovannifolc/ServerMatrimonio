package it.polito.ai.server.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.sql.Timestamp;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationToken {
    @Id
    private String id;
    private Timestamp expiryDate;
    @NotEmpty
    private String userId;
    @NotEmpty
    private String name; /*lastName*/
    @NotEmpty
    private String firstName;
    @Email
    private String email;
    @NotEmpty
    private String password;
}
