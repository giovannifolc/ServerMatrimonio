package it.polito.ai.server.controllers;

import it.polito.ai.server.services.exceptions.InvalidStudentException;
import it.polito.ai.server.services.NotificationService;
import it.polito.ai.server.services.exceptions.TokenExpiredException;
import it.polito.ai.server.services.exceptions.TokenNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class NotificationController {

    @Autowired
    NotificationService notificationService;

    /**
     * Conferma la richiesta di partecipare a un team corrispondente a un certo token
     * @param tokenId ID del token
     * Accessibile ai ruoli: Student
     */
    @GetMapping("/notification/confirm/{tokenId}")
    public void confirm(@PathVariable String tokenId){
        try{
            boolean confirmed = notificationService.confirm(tokenId);

        }catch (TokenNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        }catch (TokenExpiredException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * Rifiuta la richiesta di partecipare a un team relativa a un certo token
     * @param tokenId ID del token
     * Accessibile ai ruoli: Student
     */
    @GetMapping("/notification/reject/{tokenId}")
    public void reject(@PathVariable String tokenId){
        try{
            boolean rejected = notificationService.reject(tokenId);

        }catch (TokenNotFoundException e){

            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        }catch (TokenExpiredException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Conferma la richiesta di iscrizione relativa a un certo token
     * @param tokenId ID del token
     * Accessibile senza autenticazione.
     */
    @GetMapping("/notification/confirmRegistration/{tokenId}")
    public String confirmRegistration(@PathVariable String tokenId){
        try{
            notificationService.confirmRegistration(tokenId);
            return "<h3> La registrazione è andata a buon fine. </h3> <h4> Clicca <a href=http://localhost:4200/>qui</a> per accedere al sito </h4>";

        }catch (TokenNotFoundException | TokenExpiredException | InvalidStudentException  e){
            /*non voglio dare informazioni all'esterno su quali siano i token validi/ non validi*/
        }

        return "<h3>Ooops qualcosa è andato storto, token scaduto o inesistente!</h3>";

    }


}
