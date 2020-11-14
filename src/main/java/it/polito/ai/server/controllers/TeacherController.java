package it.polito.ai.server.controllers;

import it.polito.ai.server.dtos.CourseDTO;
import it.polito.ai.server.dtos.TeacherDTO;
import it.polito.ai.server.services.*;
import it.polito.ai.server.services.exceptions.InvalidTeacherException;
import it.polito.ai.server.services.exceptions.InvalidUserException;
import it.polito.ai.server.services.exceptions.TeacherNotFoundException;
import it.polito.ai.server.services.exceptions.TeamNotFoundException;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/API/teachers")
public class TeacherController {

    @Autowired
    TeamService teamService;

    @Autowired
    UserDetailsServiceImpl userDetailsServiceImpl;

    /**
     * Ottiene l'elenco di tutti i docenti
     * @return lista dei docenti
     * Accessibile ai ruoli: Teacher
     */
    @GetMapping({"", "/"})
    public List<TeacherDTO> all() {
        return teamService.getAllTeachers();
    }

    /**
     * Ottiene un docente dato il suo identificativo
     * @param id identificativo del docente
     * @return docente
     * Accessibile ai ruoli: Teacher
     */
    @GetMapping("/{id}")
    public TeacherDTO getOne(@PathVariable String id) {
        try {
            Optional<TeacherDTO> teacherDTO = teamService.getTeacher(id);
            if (teacherDTO.isPresent()) {
                return teacherDTO.get();
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, id);
            }
        } catch (TeamNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, id);
        }
    }

    /**
     * Aggiunge un docente al database
     * @param teacherDTO docente
     * @param br
     * Accessibile a tutti
     */
    @PostMapping({"", "/"})
    public void addTeacher(@RequestBody @Valid TeacherDTO teacherDTO, BindingResult br) {
        if (br.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            userDetailsServiceImpl.registerUser(teacherDTO);

        } catch (InvalidTeacherException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Matricola e email non corrispondono");
        } catch (InvalidUserException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
    }

    /**
     * Ottiene i corsi dato un certo identificativo docente
     * @param id identificativo docente
     * @return lista dei corsi di un docente
     * Accessibile ai ruoli: Teacher
     */
    @GetMapping("/{id}/courses")
    public List<CourseDTO> getCoursesForTeacher(@PathVariable String id) {
        try {
            return teamService.getCoursesForTeacher(id);
        } catch (TeacherNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, id);
        }
    }

    /**
     * Carica una immagine di profilo per il docente
     * @param id identificativo docente
     * @param file immagine da caricare
     * Accessibile ai ruoli: Teacher
     */
    @PostMapping("/{id}/uploadImage")
    public void uploadImage(@PathVariable String id, @RequestBody() MultipartFile file) {
        try {
            teamService.setImageForTeacher(id, ArrayUtils.toObject(file.getBytes()));
        } catch(InvalidTeacherException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (TeacherNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (IOException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Ottiene l'immagine di profilo di un docente
     * @param id identificativo docente
     * @return immagine
     * @throws IOException
     * Accessibile ai ruoli: Teacher
     */
    @GetMapping(path = {"{id}/getImage"})
    public byte[] getImage(@PathVariable String id) throws IOException {

        Byte[] img = teamService.getImageFromTeacher(id);

        if(img == null){
            File resource = new File(
                    "./src/main/resources/img/userProfile.png");
            return Base64.getEncoder().encode(ArrayUtils.toPrimitive(ArrayUtils.toObject(Files.readAllBytes(resource.toPath()))));
        }
        return Base64.getEncoder().encode(ArrayUtils.toPrimitive(teamService.getImageFromTeacher(id)));
    }
}


