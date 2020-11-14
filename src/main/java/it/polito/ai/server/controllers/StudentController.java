package it.polito.ai.server.controllers;


import it.polito.ai.server.dtos.*;
import it.polito.ai.server.entities.VirtualMachine;
import it.polito.ai.server.services.*;
import it.polito.ai.server.services.exceptions.*;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.io.*;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/API/students")
public class StudentController {

    @Autowired
    TeamService teamService;

    @Autowired
    UserDetailsServiceImpl userDetailsServiceImpl;

    /**
     * Ottiene tutti gli studenti
     * @return
     * Accessibile ai ruoli: Student
     */
    @GetMapping({"", "/"})
    public List<StudentDTO> all() {
        return teamService.getAllStudents();
    }

    /**
     * Ottiene uno studente dato il suo ID
     * @param id ID studente
     * @return studente
     * Accessibile ai ruoli: Student
     */
    @GetMapping("/{id}")
    public StudentDTO getOne(@PathVariable String id) {
        try {
            Optional<StudentDTO> studentDTO = teamService.getStudent(id);
            if (studentDTO.isPresent()) {
                return studentDTO.get();
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, id);
            }
        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, id);
        }
    }

    /**
     * Aggiunge uno studente al database
     * @param studentDTO studente
     * @param br
     */
    @PostMapping({"", "/"})
    public void addStudent(@RequestBody @Valid StudentDTO studentDTO, BindingResult br)  {
        if (br.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            userDetailsServiceImpl.registerUser(studentDTO);

        } catch (InvalidStudentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }catch (InvalidUserException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
    }


    /**
     * Ottiene l'elenco di corsi di uno studente
     * @param id ID studente
     * @return lista di corsi a cui lo studente è iscritto
     * Accessibile ai ruoli: Student
     */
    @GetMapping("/{id}/courses")
    public List<CourseDTO> getCoursesForStudent(@PathVariable String id) {
        try {
            return teamService.getCourses(id);
        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, id);
        }
    }

    /**
     * Ottiene i team di cui fa parte o a cui è stato invitato uno studente
     * @param id
     * @return lista dei team dello studente
     */
    @GetMapping("/{id}/teams")
    public List<TeamDTO> getTeamsForStudent(@PathVariable String id) {
        try {
            return teamService.getTeamsForStudent(id);
        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, id);
        }
    }

    /**
     * Ottiene i le richieste a partecipare a team dati il teamId e il nome del corso
     * @param id ID del team
     * @param courseName nome del corso
     * @return
     * Accessibile ai ruoli: Student
     */
    @GetMapping("/{id}/proposteTeams")
    public List<PropostaTeamDTO> getTeamRequests(@PathVariable String id, @RequestParam("courseName") String courseName) {
        try {
            return teamService.getTeamRequests(courseName, id);
        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, id);
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (TeamNotFoundException e) {
            /*
             * Non deve mai lanciare questa eccezione perchè se esiste un token non può non esistere il relativo team
             */
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (InvalidStudentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Carica l'immagine profilo dello studente nel database
     * @param id ID studente
     * @param file immagine da caricare
     * Accessibile ai ruoli: Student
     */
    @PostMapping("/{id}/uploadImage")
    public void uploadImage(@PathVariable String id, @RequestBody() MultipartFile file) {
        try {
            teamService.setImageForStudent(id, ArrayUtils.toObject(file.getBytes()));
        }catch(InvalidStudentException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }catch(StudentNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }catch(IOException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * Ottiene l'immagine profilo di uno studente
     * @param id id studente
     * @return immagine profilo
     * @throws IOException
     * Accessibile ai ruoli: Student
     */
    @GetMapping(path = {"{id}/getImage"})
    public byte[] getImage(@PathVariable String id) throws IOException {

        Byte[] img = teamService.getImageFromStudent(id);

        if (img == null) {
            File resource = new File(
                    "./src/main/resources/img/userProfile.png");
            return Base64.getEncoder().encode(ArrayUtils.toPrimitive(ArrayUtils.toObject(Files.readAllBytes(resource.toPath()))));
        }
        return Base64.getEncoder().encode(ArrayUtils.toPrimitive(teamService.getImageFromStudent(id)));

    }


    /**
     * Ottiene gli elaborati di uno studente per una determinata consegna
     * @param id ID studente
     * @param consegnaId ID consegna
     * @return lìsta degli elaborati dello studente relativi alla consegna
     * Accessibile ai ruoli: Student
     */
    @GetMapping("/{id}/consegne/{consegnaId}/elaborati")
    public List<ElaboratoDTO> getElaboratiForStudent(@PathVariable String id, @PathVariable String consegnaId){
         try {
             return teamService.getElaboratiForStudent(id, Long.parseLong(consegnaId));
         }catch (InvalidConsegnaException | InvalidStudentException e) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
         } catch (ConsegnaNotFoundException | StudentNotFoundException e) {
             throw new ResponseStatusException(HttpStatus.NOT_FOUND);
         }
    }

    /**
     * Ottiene un elaborato dato il suo ID
     * @param id ID studente
     * @param consegnaId ID consegna
     * @param elaboratoId ID elaborato
     * @return immagine dell'elaborato
     * Accessibile ai ruoli: Student
     */
    @GetMapping("/{id}/consegne/{consegnaId}/elaborati/{elaboratoId}")
    public byte[] getElaborato(@PathVariable String id, @PathVariable String consegnaId, @PathVariable String elaboratoId){
        try {
            Byte[] res = teamService.getElaboratoById(elaboratoId, id);
            if(res!= null){
                return Base64.getEncoder().encode(ArrayUtils.toPrimitive(res));
            }else{
                return null;
            }
        }catch (InvalidUserException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (ElaboratoNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Ottiene tutte le consegne per un dato corso.
     * @param id
     * @param courseName
     * @return
     * Accessibile ai ruoli: Student
     */

    @GetMapping("/{id}/consegne")
    public List<ConsegnaDTO> getConsegne(@PathVariable String id, @RequestParam String courseName) {
        try {
            return teamService.getConsegne(id, courseName);
        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, id);
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, courseName);
        } catch (InvalidStudentException | InvalidCourseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (InvalidUserException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Ottiene una consegna dato il suo ID
     * @param id ID studente
     * @param consegnaId ID consegna
     * @return immagine della consegna
     * Accessibile ai ruoli: Student
     */
    @GetMapping("/{id}/consegne/{consegnaId}")
    public byte[] getConsegna(@PathVariable String id, @PathVariable String consegnaId) {
        try {
            return Base64.getEncoder().encode(ArrayUtils.toPrimitive(teamService.getConsegna(Long.parseLong(consegnaId), id)));

        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, id);
        } catch (InvalidConsegnaException | InvalidStudentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (ConsegnaNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, consegnaId);
        } catch (ElaboratoNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (InvalidUserException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Notifica la lettura di una revisione da parte dello studente
     * @param id ID studente
     * @param consegnaId ID consegna
     * @param elabId ID elaborato
     * @return immagine della revisione
     * Accessibile ai ruoli: Student
     */
    @GetMapping("/{id}/consegne/{consegnaId}/correzioni/{elabId}")
    public byte[] leggiCorrezione(@PathVariable String id, @PathVariable String consegnaId, @PathVariable String elabId) {
        try {
            Byte[] res = teamService.leggiCorrezione(Long.parseLong(consegnaId), id, elabId);
            if(res!= null){
                return Base64.getEncoder().encode(ArrayUtils.toPrimitive(res));
            }else{
                return null;
            }
        }catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, id);
        } catch (InvalidConsegnaException | InvalidStudentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (ConsegnaNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, consegnaId);
        } catch (InvalidUserException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Carica un nuovo elaborato da parte dello studente per una data consegna
     * @param id ID studente
     * @param consegnaId ID consegna
     * @param elaborato elaborato da consegnare
     * @return booleano sulla riuscita consegna dell'elaborato
     * Accessibile ai ruoli: Student
     */
    @PostMapping("/{id}/consegne/{consegnaId}/elaborati")
    public Boolean consegnaElaborato(@PathVariable String id, @PathVariable String consegnaId, @RequestBody() MultipartFile elaborato) {
        try {
            return teamService.consegnaElaborato(Long.parseLong(consegnaId), id, ArrayUtils.toObject(elaborato.getBytes()));
        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, id);
        } catch (InvalidConsegnaException | InvalidStudentException | InvalidElaboratoException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (ConsegnaNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, consegnaId);
        } catch (InvalidUserException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (ElaboratoNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Il file non è stato caricato correttamente");
        }
    }

    /**
     * Crea una nuova macchina virtuale
     * @param virtualMachineDTO dati della macchina virtuale
     * @param br
     * @param id ID studente
     * @param courseName nome del corso
     * @param teamId ID del team
     * Accessibile ai ruoli: Student
     */
    @PostMapping("{id}/courses/{courseName}/teams/{teamId}/virtualMachines")
    public void createVM(@RequestBody @Valid VirtualMachineDTO virtualMachineDTO, BindingResult br,
                         @PathVariable String id, @PathVariable String courseName,
                         @PathVariable String teamId) {
        if (br.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            teamService.createVM(virtualMachineDTO, courseName, id, Long.parseLong(teamId));
        } catch (InvalidStudentException | InvalidCourseException | InvalidModelException | InvalidTeamException bre) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, id);
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, courseName);
        } catch (TeamNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, teamId);
        }
    }

    /**
     * Aggiorna i dati di una macchina virtuale
     * @param virtualMachineDTO dati della macchina virtuale
     * @param br
     * @param id ID studente
     * @param courseName nome del corso
     * @param teamId ID del team
     * @param vmId ID della macchina virtuale
     * @return macchina virtuale modificata
     * Accessibile ai ruoli: Student
     */
    @PutMapping("{id}/courses/{courseName}/teams/{teamId}/virtualMachines/{vmId}")
    public VirtualMachineDTO updateVM(@RequestBody @Valid VirtualMachineDTO virtualMachineDTO, BindingResult br, @PathVariable String id, @PathVariable String courseName,
                         @PathVariable String teamId, @PathVariable String vmId) {
        if (br.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            return teamService.manageVM(virtualMachineDTO, Long.parseLong(vmId), id, courseName);
        } catch (InvalidModelException | InvalidStudentException | InvalidCourseException | InvalidVirtualMachineException bre) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (VirtualMachineActiveException | InvalidUserException | MaxActiveVmException ae) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, id);
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, courseName);
        } catch (TeamNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, teamId);
        } catch (VirtualMachineNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, vmId);
        }
    }

    /**
     * Cancella una macchina virtuale
     * @param id ID studente
     * @param courseName nome del corso
     * @param teamId ID del team
     * @param vmId ID macchina virtuale da cancellare
     * Accessibile ai ruoli: Student
     */
    @DeleteMapping("{id}/courses/{courseName}/teams/{teamId}/virtualMachines/{vmId}")
    public void deleteVM(@PathVariable String id, @PathVariable String courseName,
                         @PathVariable String teamId, @PathVariable String vmId) {
        try {
            teamService.deleteVM(Long.parseLong(vmId), id, courseName);
        } catch (InvalidStudentException | InvalidCourseException | InvalidVirtualMachineException bre) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, id);
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, courseName);
        } catch (TeamNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, teamId);
        } catch (VirtualMachineNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, vmId);
        } catch (VirtualMachineActiveException | InvalidUserException ae) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    /**
     * Ottiene la lista degli studenti che non possiedono ancora una certa macchina virtuale
     * @param id ID studente
     * @param courseName nome del corso
     * @param teamId ID del team
     * @param vmId ID della macchina virtuale
     * @return lista degli studenti che non possiedono la macchina virtuale
     * Accessibile ai ruoli: Student
     */
    @GetMapping("{id}/courses/{courseName}/teams/{teamId}/virtualMachines/{vmId}/availableStudents")
    public List<StudentDTO> getAvailableStudentsForVM(@PathVariable String id,
                                                      @PathVariable String courseName,
                                                      @PathVariable String teamId,
                                                      @PathVariable String vmId){
        try {
            return teamService.getAvailableStudentsForVM(id, courseName, Long.parseLong(teamId), Long.parseLong(vmId));
        }catch(InvalidCourseException | InvalidTeamException | InvalidStudentException  e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }catch(VirtualMachineNotFoundException | CourseNotFoundException | TeamNotFoundException | StudentNotFoundException e ){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }catch(StudentNotEnrolledException | StudentNotInTeamException | StudentNotOwnerException | InvalidUserException e){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }


    /**
     * Condivide il possesso della macchina virtuale con una lista di studenti
     * @param ownerIds lista di IDs studenti con cui condividere il possesso
     * @param id ID studente possessore
     * @param courseName nome del corso
     * @param teamId ID del team
     * @param vmId ID della macchina virtuale
     * Accessibile ai ruoli: Student
     */
    @PostMapping("{id}/courses/{courseName}/teams/{teamId}/virtualMachines/{vmId}/owners")
    public void shareOwnershipVM(@RequestBody List<String> ownerIds, @PathVariable String id, @PathVariable String courseName,
                                 @PathVariable String teamId, @PathVariable String vmId) {
        try {
                teamService.shareOwnership(Long.parseLong(vmId), id, ownerIds);

        } catch (InvalidStudentException | InvalidCourseException | InvalidVirtualMachineException bre) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, id);
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, courseName);
        } catch (TeamNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, teamId);
        } catch (VirtualMachineNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, vmId);
        }
    }

    /**
     * Ottiene i possessori di una certa macchina virtuale
     * @param id ID studente
     * @param courseName nome del corso
     * @param teamId ID del team
     * @param vmId ID della macchina virtuale
     * @return lista degli IDs dei possessori
     * Accessibile ai ruoli: Student
     */
    @GetMapping("{id}/courses/{courseName}/teams/{teamId}/virtualMachines/{vmId}/owners")
    public List<String> getOwnersVM(@PathVariable String id, @PathVariable String courseName,
                                    @PathVariable String teamId, @PathVariable String vmId) {
        try {
            return teamService.getOwners(Long.parseLong(vmId), id);

        } catch (InvalidStudentException | InvalidCourseException | InvalidVirtualMachineException bre) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, id);
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, courseName);
        } catch (TeamNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, teamId);
        } catch (VirtualMachineNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, vmId);
        } catch (InvalidUserException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Ottiene l'elenco delle macchine virtuali possedute da un certo studente
     * @param id ID studente
     * @param courseName nome del corso
     * @param teamId ID del team
     * @return lista degli IDs delle macchine virtuali possedute
     * Accessibile ai ruoli: Student
     */
    @GetMapping("{id}/courses/{courseName}/teams/{teamId}/virtualMachines/owned")
    public List<Long> getOwnedVM(@PathVariable String id, @PathVariable String courseName,
                                 @PathVariable String teamId) {
        try {
            return teamService.getOwnedVM(id, courseName, Long.parseLong(teamId));

        } catch (InvalidStudentException | InvalidCourseException bre) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (InvalidUserException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, id);
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, courseName);
        } catch (TeamNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, teamId);
        }

    }

    /**
     * Controlla se uno studente ha già accettato una richiesta per un team di un certo corso
     * @param id ID studente
     * @param courseName nome del corso
     * @return true se ha già accettato una richiesta, false altrimenti
     * Accessibile ai ruoli: Student
     */
    @GetMapping("{id}/courses/{courseName}/teams/acceptedRequest")
    public boolean acceptedRequest(@PathVariable String id, @PathVariable String courseName) {
        try {
            return teamService.checkAcceptedRequest(id,courseName);

        } catch (InvalidStudentException | InvalidCourseException | InvalidTeamException bre) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (InvalidUserException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, id);
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, courseName);
        }

    }

    /**
     * Ottiene l'immagine corrispondente a una certa macchina virtuale
     * @param id ID studente
     * @param courseName nome del corso
     * @param teamId ID del team
     * @param vmId ID della macchina virtuale
     * @return immagine della macchina virtuale
     * Accessibile ai ruoli: Student
     */
    @GetMapping("/{id}/courses/{courseName}/teams/{teamId}/virtualMachines/{vmId}")
    public byte[] getVM(@PathVariable String id, @PathVariable String courseName,
                                @PathVariable String teamId, @PathVariable String vmId) {
        try {
            return teamService.getVM();
        } catch (IOException ioe) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore nella restituzione della VM");
        }

    }

    /**
     * Ottiene i dati di tutte le macchine virtuali associate a un certo team
     * @param id ID studente
     * @param courseName nome del corso
     * @param teamId ID del team
     * @return lista dei dati delle macchine virtuali
     * Accessibile ai ruoli: Student
     */
    @GetMapping("{id}/courses/{courseName}/teams/{teamId}/virtualMachines")
    public List<VirtualMachineDTO> getVMs(@PathVariable String id, @PathVariable String courseName,
                                          @PathVariable String teamId) {
        try {
            return teamService.getVirtualMachinesForStudent(id, Long.parseLong(teamId));

        } catch (InvalidStudentException | InvalidCourseException | InvalidTeamException bre) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (InvalidUserException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, id);
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, courseName);
        } catch (TeamNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, teamId);
        }
    }

    /**
     * Ottiene il team di uno studente per un certo corso
     * @param id ID studente
     * @param courseName nome del corso
     * @return dati del team
     * Accessibile ai ruoli: Student
     */
    @GetMapping("{id}/courses/{courseName}/team")
    public TeamDTO getTeamForStudentAndCourse(@PathVariable String id, @PathVariable String courseName) {
        try {
            return teamService.getTeamForStudentAndCourse(id, courseName);
        } catch (InvalidUserException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, id);
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, courseName);
        }

    }

}
