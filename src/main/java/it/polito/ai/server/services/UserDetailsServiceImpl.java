package it.polito.ai.server.services;

import it.polito.ai.server.dtos.StudentDTO;
import it.polito.ai.server.dtos.TeacherDTO;
import it.polito.ai.server.dtos.UserDTO;
import it.polito.ai.server.entities.RegistrationToken;
import it.polito.ai.server.entities.User;
import it.polito.ai.server.repositories.RegistrationTokenRepository;
import it.polito.ai.server.repositories.UserRepository;
import it.polito.ai.server.services.exceptions.InvalidStudentException;
import it.polito.ai.server.services.exceptions.InvalidTeacherException;
import it.polito.ai.server.services.exceptions.InvalidUserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder bcryptEncoder;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RegistrationTokenRepository registrationTokenRepository;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        if(s==null){
        throw new UsernameNotFoundException("");
        }
        if(!userRepository.findByUsername(s).isPresent()){
        throw new UsernameNotFoundException("");
        }
        User user =userRepository.findByUsername(s).get();
        List<GrantedAuthority> roles =new ArrayList<>();
        roles.add(new SimpleGrantedAuthority(user.getRole()));

        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), roles);
    }

    /*
       Manda la richiesta la mail per completare la registrazione.
     */
    public void registerUser(TeacherDTO teacherDTO){

        if(userRepository.existsByUsername(teacherDTO.getId())){
            throw new InvalidUserException();
        }
        Instant instant = Instant.now();
        Timestamp scadenza = Timestamp.from(instant.plus(Long.parseLong("60"), ChronoUnit.MINUTES));

        String address = "d" + teacherDTO.getId() + "@polito.it";
        if( !address.equals(teacherDTO.getEmail())) throw new InvalidTeacherException();

        RegistrationToken rToken = RegistrationToken.builder().id(UUID.randomUUID().toString())
                .expiryDate(scadenza)
                .userId(teacherDTO.getId())
                .email(teacherDTO.getEmail())
                .firstName(teacherDTO.getFirstName())
                .name(teacherDTO.getName())
                .password(bcryptEncoder.encode(teacherDTO.getPassword())).build();

        registrationTokenRepository.save(rToken);

        String subject= "Conferma registrazione al sistema";
        String corpo = "<p>Questo è il tuo username: <b><i><em>d"+teacherDTO.getId()+"</em></i></b></p>" +
                    "<p>Per confermare la tua registrazione clicca qui: <a> http://localhost:8080/notification/confirmRegistration/" + rToken.getId() ;


        //studente o docente
        notificationService.sendMessage(address, subject, corpo);

    }

    public void registerUser(StudentDTO studentDTO){

        if(userRepository.existsByUsername(studentDTO.getId())){
            throw new InvalidUserException();
        }
        Instant instant = Instant.now();
        Timestamp scadenza = Timestamp.from(instant.plus(Long.parseLong("60"), ChronoUnit.MINUTES));

        String address = "s" + studentDTO.getId() + "@studenti.polito.it";
        if( !address.equals(studentDTO.getEmail())) throw new InvalidStudentException();

        RegistrationToken rToken = RegistrationToken.builder().id(UUID.randomUUID().toString())
                .expiryDate(scadenza)
                .userId(studentDTO.getId())
                .email(studentDTO.getEmail())
                .firstName(studentDTO.getFirstName())
                .name(studentDTO.getName())
                .password(bcryptEncoder.encode(studentDTO.getPassword())).build();

        registrationTokenRepository.save(rToken);

        String subject= "Conferma registrazione al sistema";
        String corpo = "<p>Questo è il tuo username: <b><i><em>s"+studentDTO.getId()+"</em></i></b></p>" +
                "<p>Per confermare la tua registrazione clicca qui: <a> http://localhost:8080/notification/confirmRegistration/" + rToken.getId() ;


        //studente o docente
        notificationService.sendMessage(address, subject, corpo);


    }

    public void addUser(UserDTO userDTO){
        if(userDTO == null){
            return;
        }
        if(userDTO.getId()==null){
            return;
        }
        if(userDTO.getRole()==null){
            return;
        }
        if(userRepository.existsByUsername(userDTO.getId())){
            /*non aggiungo due utenti con lo stesso username*/
            throw new InvalidUserException();
        }

        String address;

        if(userDTO.getRole().equals("ROLE_STUDENT")) {
            address = "s" + userDTO.getId() + "@studenti.polito.it";
            if( !address.equals(userDTO.getEmail())) throw new InvalidUserException();
        } else if(userDTO.getRole().equals("ROLE_TEACHER")) {
            address = "d" + userDTO.getId() + "@polito.it";
            if( !address.equals(userDTO.getEmail())) throw new InvalidUserException();
        }

        /**
         * todo: Controllo sintassi password, check identità passwords
         */



        userRepository.save( User.builder()
                .username(userDTO.getId())
                .password(userDTO.getPassword())
                .email(userDTO.getEmail())
                .role(userDTO.getRole())
                .build());
    }

    /*
    private String generateRandomPassword() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generatedString;
    }

     */
}
