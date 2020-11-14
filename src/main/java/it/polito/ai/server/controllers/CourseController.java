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
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/API/courses")
public class CourseController {

    @Autowired
    TeamService teamService;

    /**
     * Ottiene tutti i corsi
     * @return lista di tutti i corsi
     * Accessibile ai ruoli: Teacher, Student
     */
    @GetMapping({"", "/"})
    public List<CourseDTO> all() {
        return teamService.getAllCourses();
    }

    /**
     * Ottiene i dati di un corso tramite il suo nome
     * @param name nome del corso
     * @return dati del corso
     * Accessibile ai ruoli: Teacher, Student
     */
    @GetMapping("/{name}")
    public CourseDTO getOne(@PathVariable String name) {
        try {
            Optional<CourseDTO> courseDTO = teamService.getCourse(name);
            if (courseDTO.isPresent()) {
                return courseDTO.get();
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, name);
            }
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, name);
        }
    }

    /**
     * Aggiornamento dei dati del corso
     * @param name nome del corso
     * @param courseDTO dati del corso
     * @param br
     * Accessibile ai ruoli: Teacher
     */
    @PutMapping("/{name}")
    public void updateCourse(@PathVariable String name, @RequestBody @Valid CourseDTO courseDTO, BindingResult br) {
        if (br.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (!name.equals(courseDTO.getName())) {
            /*sto cercando di modificare lo status di un corso diverso da quello indicato nell'url*/
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            Optional<CourseDTO> courseDTOOptional = teamService.getCourse(name);
            if (!courseDTOOptional.isPresent()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, name);
            }
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, name);
        }

        try {
            teamService.updateCourse(courseDTO);
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, name);
        }catch (InvalidTeacherException e){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }catch (InvalidCourseException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Aggiorna lo stato di un corso
     * @param name nome del corso
     * @param courseDTO dati del corso
     * @param br
     * @return dati del corso aggiornati
     * Accessibile ai ruoli: Teacher
     */
    @PutMapping("/{name}/enable")
    public CourseDTO updateCourseStatus(@PathVariable String name, @RequestBody @Valid CourseDTO courseDTO, BindingResult br) {
        if (br.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (!name.equals(courseDTO.getName())) {
            /*sto cercando di modificare lo status di un corso diverso da quello indicato nell'url*/
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            Optional<CourseDTO> courseDTOOptional = teamService.getCourse(name);
            if (!courseDTOOptional.isPresent()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, name);
            }
            CourseDTO existingCourse = courseDTOOptional.get();
            if (existingCourse.getMax() != courseDTO.getMax()
                    || existingCourse.getMin() != courseDTO.getMin()
                    || !existingCourse.getAcronimo().equals(courseDTO.getAcronimo())) {
                /*ho cercato di modificare qualcosa diverso dallo stato*/
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, name);
        }

        try {
            boolean toEnable = courseDTO.isEnabled();
            if (toEnable) {


                teamService.enableCourse(name);
            } else {
                teamService.disableCourse(name);
            }
            return courseDTO;
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, name);
        }
    }

    /**
     * Ottiene la lista degli studenti iscritti a un certo corso
     * @param name nome del corso
     * @return elenco degli studenti
     * Accessibile ai ruoli: Teacher
     */
    @GetMapping("/{name}/enrolled")
    public List<StudentWithTeamInfoDTO> enrolledStudents(@PathVariable String name) {
        try {
            return new ArrayList<>(teamService.getEnrolledStudentsWithTeamInfo(name));

        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, name);
        }
    }

    /**
     * Ottiene la lista degli studenti non iscritti a un certo corso
     * @param name nome del corso
     * @return elenco degli studenti
     * Accessibile ai ruoli: Teacher
     */
    @GetMapping("/{name}/notEnrolled")
    public List<StudentDTO> notEnrolledStudents(@PathVariable String name) {
        try {
            return teamService.getNotEnrolledStudents(name);

        } catch (CourseNotFoundException | InvalidCourseException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, name);
        }
    }

    /**
     * Disiscrive uno studente da un corso
     * @param name nome del corso
     * @param map contiene l'ID dello studente da disiscrivere
     * Accessibile ai ruoli: Teacher
     */
    @PostMapping("/{name}/unEnrollOne")
    @ResponseStatus(value = HttpStatus.OK)
    public void unenrollOne(@PathVariable String name, @RequestBody Map<String, String> map) {
        if (map.keySet().size() != 1 || !map.containsKey("id")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            boolean notEnrolled = teamService.removeStudentFromCourse(map.get("id"), name);
            if (!notEnrolled) {
                throw new ResponseStatusException(HttpStatus.CONFLICT);
            }
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, name);
        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, map.get("id"));
        } catch (CourseDisabledException | InvalidTeacherException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

    }

    /**
     * Aggiunge un corso
     * @param parametriDTO parametri del corso, contiene i dati del corso e il modello delle macchine virtuali
     * @param br
     * @return dati del corso
     * Accessibile ai ruoli: Teacher
     */
    @PostMapping({"", "/"})
    public CourseDTO addCourse(@RequestBody @Valid ParametriDTO parametriDTO, BindingResult br) {
        if (br.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            boolean added = teamService.addCourse(parametriDTO.getCourseDTO(), parametriDTO.getModelloVMDTO());
            if (added) {
                return parametriDTO.getCourseDTO();
            } else {
                throw new ResponseStatusException(HttpStatus.CONFLICT, parametriDTO.getCourseDTO().getName());
            }
        } catch (InvalidCourseException | InvalidModelException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (TeacherNotFoundException nfe) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    /**
     * Aggiorna il modello delle macchine virtuali di un corso
     * @param name nome del corso
     * @param modelloVMDTO modello delle macchine virtuali
     * @param br
     * @return  modello delle macchine virtuali aggiornati
     * Accessibile ai ruoli: Teacher
     */
    @PutMapping("{name}/modelloVM")
    public ModelloVMDTO updateModelloVM(@PathVariable String name, @RequestBody @Valid ModelloVMDTO modelloVMDTO, BindingResult br) {

        if (br.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            boolean updated = teamService.updateModelloVM(name, modelloVMDTO);
            if (updated) {
                return modelloVMDTO;
            } else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        } catch (InvalidCourseException | InvalidModelException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (InvalidTeacherException | ForbiddenOperationException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, name);
        }
    }

    /**
     * Iscrive uno studente a un certo corso
     * @param name nome del corso
     * @param map contiene l'ID dello studente da iscrivere
     * Accessibile ai ruoli: Teacher
     */
    @PostMapping("{name}/enrollOne")
    @ResponseStatus(value = HttpStatus.OK)
    public void enrollOne(@PathVariable String name, @RequestBody Map<String, String> map) {
        if (map.keySet().size() != 1 || !map.containsKey("id")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            boolean enrolled = teamService.addStudentToCourse(map.get("id"), name);
            if (!enrolled) {
                throw new ResponseStatusException(HttpStatus.CONFLICT);
            }
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, name);
        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, map.get("id"));
        } catch (CourseDisabledException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

    }

    /**
     * Aggiungi un docente per un corso
     * @param name nome del corso
     * @param map contiene l'ID del docente che deve essere aggiunto
     * Accessibile ai ruoli: Teacher
     */
    @PostMapping("{name}/addTeacher")
    @ResponseStatus(value = HttpStatus.OK)
    public void addTeacherForCourse(@PathVariable String name, @RequestBody Map<String, String> map) {
        if (map.keySet().size() != 1 || !map.containsKey("id")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            teamService.addTeacherForCourse(map.get("id"), name);
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, name);
        } catch (TeacherNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, map.get("id"));
        } catch (InvalidCourseException | InvalidTeacherException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Iscrive più studenti a un certo corso tramite un file CSV
     * @param name nome del corso
     * @param file file CSV contenente gli studenti da iscrivere
     * @return lista di booleani, per ogni studente se è stato iscritto correttamente ritorna true
     * Accessibile ai ruoli: Teacher
     */
    @PostMapping("{name}/enrollMany")
    public List<Boolean> enrollStudents(@PathVariable String name, @RequestParam("file") MultipartFile file) {
        try {
            Reader r = new BufferedReader(new InputStreamReader(file.getInputStream()));
            return teamService.enrollAll(r, name);
        } catch (InvalidReaderException | IOException | InvalidStudentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, name);
        } catch (CourseDisabledException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }

    }

    /**
     * Ottiene i team relativi a un certo corso
     * @param name nome del corso
     * @return lista dei team
     * Accessibile ai ruoli: Teacher
     */
    @GetMapping("/{name}/teams")
    public List<TeamDTO> getTeamsForCourse(@PathVariable String name) {
        try {
            return teamService.getTeamsForCourse(name);
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, name);
        }
    }

    /**
     * Ottiene gli studenti già iscritti in teams per un certo corso
     * @param name nome del corso
     * @return lista di studenti
     * Accessibile ai ruoli: Teacher, Student
     */
    @GetMapping("/{name}/alreadyTakenStudents")
    public List<StudentDTO> getStudentsInTeams(@PathVariable String name) {
        try {
            return teamService.getStudentsInTeams(name);
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, name);
        }
    }

    /**
     * Ottiene studenti non iscritti a teams per un certo corso
     * @param name nome del corso
     * @return lista degli studenti disponibili
     * Accessibile ai ruoli: Teacher, Student
     */
    @GetMapping("/{name}/availableStudents")
    public List<StudentDTO> getAvailableStudents(@PathVariable String name) {
        try {
            return teamService.getAvailableStudents(name);
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, name);
        }
    }

    /**
     * Ottiene tutti i docenti che tengono un certo corso
     * @param name nome del corso
     * @return lista dei docenti
     * Accessibile ai ruoli: Teacher, Student
     */
    @GetMapping("/{name}/teachers")
    public List<TeacherDTO> getTeachersForCourse(@PathVariable String name) {
        try {
            List<TeacherDTO> teacherDTOs = teamService.getTeachersForCourse(name);
            if (!teacherDTOs.isEmpty()) {
                return teacherDTOs;
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        } catch (CourseNotFoundException | InvalidCourseException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, name);
        }
    }

    /**
     * Propone un team con un elenco di studenti
     * @param name nome del corso
     * @param map contiene "teamName" : nome del team, "memberIds" : IDs dei membri, "minScadenza" : minuti concessi per la conferma
     * @return
     * Accessibile ai ruoli: Student
     */
    @PostMapping("{name}/proposeTeam")
    public TeamDTO proposeTeam(@PathVariable String name, @RequestBody Map<String, Object> map) {
        /*
          expiryDate è in ore
         */

        if (map.keySet().size() != 3 || !map.containsKey("teamName") || !map.containsKey("memberIds") || !map.containsKey("oreScadenza")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        try {
            String teamName = (String) map.get("teamName");
            Instant instant = Instant.now().plus(Long.parseLong(map.get("oreScadenza").toString()), ChronoUnit.HOURS);
            Timestamp expiryDate = Timestamp.from(instant);
            if (expiryDate.before(new Timestamp(System.currentTimeMillis()))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
            List<String> memberIds = (List<String>) map.get("memberIds");
            return teamService.proposeTeam(name, teamName, memberIds, expiryDate);
        } catch (ForbiddenOperationException | CourseDisabledException | NotEnoughStudentsException | TooManyStudentsException | StudentNotEnrolledException | StudentAlreadyInATeamException e) {

            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (ClassCastException | DuplicateMemberException | NumberFormatException e) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (CourseNotFoundException | InvalidCourseException e) {

            throw new ResponseStatusException(HttpStatus.NOT_FOUND, name);
        } catch (StudentNotFoundException e) {

            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (InvalidTeamNameException e) {

            throw new ResponseStatusException(HttpStatus.CONFLICT, "Il nome del Team è null o già occupato.");
        }
    }

    /**
     * Crea una nuova consegna per un certo corso
     * @param file immagine della consegna
     * @param name nome del corso
     * @param scadenza scadenza della consegna
     * @param consegna nome della consegna
     * Accessibile ai ruoli: Teacher
     */
    @PostMapping("{name}/consegne")
    public void createConsegna(@RequestBody MultipartFile file, @PathVariable String name, @RequestParam("scadenza") String scadenza, @RequestParam("consegna") String consegna) {
        try {

            if (scadenza == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insert scadenza");
            }

            teamService.createConsegna(scadenza, name, consegna, ArrayUtils.toObject(file.getBytes()));

        } catch (ParseException | IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expiration must have format: dd/mm/yyyy");
        } catch (CourseNotFoundException cnf) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found");
        } catch (TeacherNotFoundException tnf) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found");
        } catch (InvalidCourseException | InvalidConsegnaException | InvalidTeacherException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * Ottiene le consegne relative a un corso
     * @param courseName nome del corso
     * @return lista delle consegne
     * Accessibile ai ruoli: Teacher, Student
     */
    @GetMapping("/{courseName}/consegne")
    public List<ConsegnaDTO> getConsegneForCourse(@PathVariable String courseName) {
        try {
            return teamService.getConsegneForCourse(courseName);
        } catch (InvalidCourseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found");
        }

    }

    /**
     * Ottiene immagine di una consegna
     * @param courseName nome del corso
     * @param id ID della consegna
     * @return immagine della consegna
     * Accessibile ai ruoli: Teacher, Student
     */
    @GetMapping("/{courseName}/consegne/{id}")
    public byte[] getConsegnaForCourse(@PathVariable String courseName, @PathVariable String id) {
        try {
            Byte[] res =  teamService.getConsegnaForCourse(id);
            return Base64.getEncoder().encode(ArrayUtils.toPrimitive(res));
        } catch (InvalidConsegnaException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (ConsegnaNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Consegna not found");
        }

    }

    /**
     * Ottiene gli elaborati relativi a una consegna
     * @param courseName nome del corso
     * @param consegnaId ID della consegna
     * @return
     * Accessibile ai ruoli: Teacher
     */
    @GetMapping("/{courseName}/consegne/{consegnaId}/elaborati")
    public List<ElaboratoForTeacherDTO> getElaborati(@PathVariable String courseName, @PathVariable String consegnaId) {
        try {
            return teamService.getElaborati(courseName, Long.parseLong(consegnaId));
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, courseName);
        } catch (InvalidCourseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, courseName);
        } catch (InvalidConsegnaException | InvalidTeacherException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (ConsegnaNotFoundException | TeacherNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

    }

    /**
     * Ottiene solo l'ultimo elaborato relativo a una certa consegna per ogni studente
     * @param courseName nome del corso
     * @param consegnaId ID della consegna
     * @return lista di elaborati
     * Accessibile ai ruoli: Teacher
     */
    @GetMapping("/{courseName}/consegne/{consegnaId}/ultimiElaborati")
    public List<ElaboratoDTO> getUltimiElaborati(@PathVariable String courseName, @PathVariable String consegnaId) {
        try {
            return teamService.getUltimiElaborati(courseName, Long.parseLong(consegnaId));
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, courseName);
        } catch (InvalidCourseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, courseName);
        } catch (InvalidConsegnaException | InvalidTeacherException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (ConsegnaNotFoundException | TeacherNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

    }

    /**
     * Ottiene gli elaborati consegnati per una certa consegna da un certo studente
     * @param courseName nome del corso
     * @param consegnaId ID della consegna
     * @param studentId ID dello studente
     * @return lista degli elaborati
     * Accessibile ai ruoli: Teacher
     */
    @GetMapping("/{courseName}/students/{studentId}/consegne/{consegnaId}/elaborati")
    public List<ElaboratoDTO> getElaboratiConsegnatiFromStudentId(@PathVariable String courseName,
                                                                  @PathVariable String consegnaId,
                                                                  @PathVariable String studentId) {
        try {
            return teamService.getElaboratiConsegnatiFromStudentId(courseName, Long.parseLong(consegnaId), studentId);
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, courseName);
        } catch (InvalidCourseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, courseName);
        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, studentId);
        } catch (InvalidStudentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, studentId);
        } catch (InvalidConsegnaException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (ConsegnaNotFoundException | TeacherNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (InvalidTeacherException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

    }

    /**
     * Ottiene l'immagine relativa a un certo elaborato
     * @param courseName nome del corso
     * @param consegnaId ID della consegna
     * @param elaboratoId ID dell'elaborato
     * @param studentId ID dello studente
     * @return immagine
     * Accessibile ai ruoli: Teacher
     */
    @GetMapping("/{courseName}/students/{studentId}/consegne/{consegnaId}/elaborati/{elaboratoId}")
    public byte[] getElaborato(@PathVariable String courseName,
                               @PathVariable String consegnaId,
                               @PathVariable String elaboratoId,
                               @PathVariable String studentId) {
        try {

            Byte[] res =teamService.getElaborato(Long.parseLong(elaboratoId), courseName, studentId, Long.parseLong(consegnaId));
            if(res!=null){
                return Base64.getEncoder().encode(ArrayUtils.toPrimitive(res));
            }else{
                return null;
            }
        } catch (StudentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, studentId);
        } catch (InvalidConsegnaException | InvalidElaboratoException | InvalidTeacherException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (ConsegnaNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, consegnaId);
        } catch (InvalidStudentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        } catch (ElaboratoNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (InvalidCourseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, courseName);
        }
    }

    /**
     * Permette di caricare una revisione di un elaborato
     * @param file immagine relativa alla revisione
     * @param elaboratoId ID dell'elaborato che si vuole correggere
     * @param courseName nome del corso
     * @param consegnaId ID della consegna
     * @param voto voto assegnato (numero o null)
     * @param possibileRiconsegna permettere la riconsegna o meno (true o false)
     * Accessibile ai ruoli: Teacher
     */
    @PostMapping("/{courseName}/consegne/{consegnaId}/elaborati/{elaboratoId}")
    public void correggiElaborato(@RequestBody() MultipartFile file,
                                  @PathVariable String elaboratoId,
                                  @PathVariable String courseName,
                                  @PathVariable String consegnaId,
                                  @RequestParam("voto") String voto,
                                  @RequestParam("possibileRiconsegna") String possibileRiconsegna) {

        try {
            if (possibileRiconsegna == null || voto == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "I parametri non possono essere null");
            }

            if(!possibileRiconsegna.toLowerCase().equals("true") && !possibileRiconsegna.toLowerCase().equals("false") || file==null ){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }

            if ((possibileRiconsegna.toLowerCase().equals("true") && !voto.equals("-")) || (possibileRiconsegna.toLowerCase().equals("false") && voto.equals("-"))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }

            teamService.correggereElaborato(Long.parseLong(elaboratoId), courseName,
                    Long.parseLong(consegnaId), ArrayUtils.toObject(file.getBytes()),
                    possibileRiconsegna.toLowerCase(), voto);

        } catch (ElaboratoNotFoundException | ConsegnaNotFoundException | StudentNotFoundException
                | TeacherNotFoundException | CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (InvalidVotoException | InvalidCourseException | InvalidConsegnaException | InvalidStudentException | InvalidTeacherException iv) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (InvalidElaboratoException env) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Elaborato già revisionato, ancora non consegnato o fuori tempo massimo");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Il metodo non va a prendere l'immagine dal database come per esempio per l'immagine dei professori o degli studenti
     *  ma dal file system
     * Accessibile ai ruoli: Teacher, Student
     */
    @GetMapping("/{courseName}/teams/{teamId}/virtualMachines/{vmId}")
    public byte[] getVM() {
        try {
            return teamService.getVM();
        } catch (IOException ioe) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore nella restituzione della VM");
        }

    }

    /**
     * Ottiene le macchine virtuali di un dato team
     * @param courseName nome del corso
     * @param teamId ID del team
     * @return lista di macchine virtuali
     * Accessibile ai ruoli: Teacher
     */
    @GetMapping("{courseName}/teams/{teamId}/virtualMachines")
    public List<VirtualMachineDTO> getVMs(@PathVariable String courseName,
                                          @PathVariable String teamId) {
        try {
            return teamService.getVirtualMachinesForTeacher(courseName, Long.parseLong(teamId));

        } catch (InvalidStudentException | InvalidCourseException bre) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (StudentNotFoundException | CourseNotFoundException | TeamNotFoundException nfe) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (InvalidUserException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Cancella un corso
     * @param courseName nome del corso
     * Accessibile ai ruoli: Teacher
     */
    @DeleteMapping("{courseName}")
    public void deleteCourse(@PathVariable String courseName ){
        try{
            teamService.deleteCourse(courseName);
        }catch(InvalidCourseException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }catch (CourseNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }catch (InvalidTeacherException e){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    /**
     * Ottiene il modello di macchine virtuali relativo a un certo corso
     * @param courseName nome del corso
     * @return modello di macchine virtuali
     * Accessibile ai ruoli: Teacher, Student
     */
    @GetMapping("{courseName}/modelloVM")
    public ModelloVMDTO getModelloVM(@PathVariable String courseName) {
        try {
            return teamService.getModelloVM(courseName);
        } catch (CourseNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (InvalidCourseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}
