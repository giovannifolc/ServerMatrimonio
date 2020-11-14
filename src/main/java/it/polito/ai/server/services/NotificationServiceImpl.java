package it.polito.ai.server.services;

import it.polito.ai.server.dtos.StudentDTO;
import it.polito.ai.server.dtos.TeacherDTO;
import it.polito.ai.server.dtos.TeamDTO;
import it.polito.ai.server.entities.RegistrationToken;
import it.polito.ai.server.entities.Student;
import it.polito.ai.server.entities.Token;
import it.polito.ai.server.repositories.RegistrationTokenRepository;
import it.polito.ai.server.repositories.TokenRepository;
import it.polito.ai.server.services.exceptions.InvalidUserException;
import it.polito.ai.server.services.exceptions.TeamNotFoundException;
import it.polito.ai.server.services.exceptions.TokenExpiredException;
import it.polito.ai.server.services.exceptions.TokenNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.*;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private TeamService teamService;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private RegistrationTokenRepository registrationTokenRepository;

    @Override
    public void sendMessage(String address, String subject, String body) {
        if(address == null || subject ==null || body ==null){
            return;
        }

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = null;
        try {
            helper = new MimeMessageHelper(message, true);
            helper.setTo(address);
            helper.setSubject(subject);
            helper.setText(body, true);
          /*
	    Rimuovere i commenti se si vuole testare la funzione di registrazione tramite email
            javaMailSender.send(message);
	   */

        } catch (MessagingException ignored) {

        }



    }

    @Override
    public boolean confirm(String token) {
        if(token==null){
            throw  new TokenNotFoundException();
        }
        if(!tokenRepository.findById(token).isPresent()){
            throw  new TokenNotFoundException();
        }
        Token t = tokenRepository.findById(token).get();
        if(t.getExpiryDate().before(new Timestamp(System.currentTimeMillis()))) {
            throw new TokenExpiredException();
        }
        /*rimuovo token dalla tabella*/
        tokenRepository.delete(t);
        List<Token> danglingTokens = tokenRepository.findAllByTeamId(t.getTeamId());
        if(danglingTokens.size()==0){
            /*ultima conferma*/
            try{
            teamService.activateTeam(t.getTeamId());
            return true;
            }catch (TeamNotFoundException e){
                return false;
            }
        }
        else{
        return false;
        }
    }

    @Override
    public boolean reject(String token) {
        if(token==null){
            throw  new TokenNotFoundException();
        }
        if(!tokenRepository.findById(token).isPresent()){
            /*sia perchè non è mai esistito sia perchè esisteva ed è stato cancellato*/
            return false;
        }
        Token t = tokenRepository.findById(token).get();
        if(t.getExpiryDate().before(new Timestamp(System.currentTimeMillis()))) {
            throw new TokenExpiredException();
        }
        List<Token> danglingTokens = tokenRepository.findAllByTeamId(t.getTeamId());
        for(Token tk : danglingTokens){
            tokenRepository.delete(tk);
        }
        try{
            teamService.evictTeam(t.getTeamId());
            return true;
        }catch (TeamNotFoundException e){
            return false;
        }
    }

    @Override
    public void notifyTeam(TeamDTO dto, List<Student> students, Timestamp expiryDate) {
        if(dto == null || students == null){
            return;
        }
        /*creo un token per ogni membro del team*/
        List<Token> tokens = new ArrayList<>();
        for(Student s :  students){

            tokens.add(
                    Token.builder()
                            .id(UUID.randomUUID().toString())
                            .expiryDate(expiryDate)
                            .teamId(dto.getId())
                            .student(s)
                            .build()
                    );
        }

        for(Token t : tokens) {
            tokenRepository.save(t);
        }
    }

    @Override
    public void confirmRegistration(String token) {
        if(token==null){
            throw  new TokenNotFoundException();
        }
        if(!registrationTokenRepository.findById(token).isPresent()){
            throw  new TokenNotFoundException();
        }
        RegistrationToken t = registrationTokenRepository.findById(token).get();

        if(t.getExpiryDate().before(new Timestamp(System.currentTimeMillis()))) {
            throw new TokenExpiredException();
        }

        if (t.getEmail().split("@")[1].equals("studenti.polito.it")) {
            StudentDTO sDTO = StudentDTO.builder().id(t.getUserId())
                    .email(t.getEmail())
                    .firstName(t.getFirstName())
                    .name(t.getName())
                    .password(t.getPassword()).build();

            teamService.addStudent(sDTO);
        } else {
            if (t.getEmail().split("@")[1].equals("polito.it")) {
                TeacherDTO tDTO = TeacherDTO.builder().id(t.getUserId())
                        .email(t.getEmail())
                        .firstName(t.getFirstName())
                        .name(t.getName())
                        .password(t.getPassword()).build();
                teamService.addTeacher(tDTO);
            } else {
                throw new InvalidUserException();
            }
        }



        /*rimuovo token dalla tabella*/
        registrationTokenRepository.delete(t);

    }

  @Scheduled(fixedDelay = 10000)
    public void deleteExpiredTeams(){
        List<Token> expiredTokens = tokenRepository.findAllByExpiryDateBefore(new Timestamp(System.currentTimeMillis()));
        Set<Long> expiredTeamIds = new HashSet<>(); //in modo che la add di un duplicato non abbia effetto
        for(Token token : expiredTokens){
            expiredTeamIds.add(token.getTeamId());
        }
        tokenRepository.deleteAll(expiredTokens);
        teamService.evictTeams(new ArrayList<>(expiredTeamIds)); //non lancia eccezioni
    }

    @Scheduled(fixedDelay = 10000)
    public void deleteExpiredRegistrationToken(){
        List<RegistrationToken> expiredTokens = registrationTokenRepository
                .findAllByExpiryDateBefore(new Timestamp(System.currentTimeMillis()));
        registrationTokenRepository.deleteAll(expiredTokens);
    }


}
