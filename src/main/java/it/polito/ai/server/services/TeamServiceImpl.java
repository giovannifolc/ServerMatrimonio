package it.polito.ai.server.services;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import it.polito.ai.server.dtos.*;
import it.polito.ai.server.entities.*;
import it.polito.ai.server.repositories.*;
import it.polito.ai.server.services.exceptions.*;
import org.apache.commons.lang3.ArrayUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.io.Reader;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TeamServiceImpl implements TeamService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private ModelloVMRepository modelloVMRepository;

    @Autowired
    private ConsegnaRepository consegnaRepository;

    @Autowired
    private ElaboratoRepository elaboratoRepository;

    @Autowired
    private VMRepository vmRepository;

    private void checkAuthorizationId(String userId) {

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!userId.equals(userDetails.getUsername())) {
            throw new InvalidUserException();
        }

    }


    private void checkTeacherId(String teacherId) {
        if (teacherId == null) {
            throw new InvalidTeacherException();
        }

        if (!teacherRepository.existsById(teacherId)) {
            throw new TeacherNotFoundException();
        }
    }

    //rilancia InvalidStudentException e StudentNotFoundException
    private void checkStudentId(String studentId) {
        if (studentId == null) {
            throw new InvalidStudentException();
        }

        if (!studentRepository.existsById(studentId)) {
            throw new StudentNotFoundException();
        }
    }

    //rilancia InvalidCourseException e CourseNotFoundException
    private void checkCourseName(String courseName) {
        if (courseName == null) {
            throw new InvalidCourseException();
        }
        if (!courseRepository.existsById(courseName)) {
            throw new CourseNotFoundException();
        }
    }


    private void checkTeamId(Long teamId) {
        if (teamId == null) {
            throw new InvalidTeamException();
        }
        if (!teamRepository.existsById(teamId)) {
            throw new TeamNotFoundException();
        }
    }

    //rilancia InvalidElaboratoException e ElaboratoNotFoundException
    private void checkElaboratoId(Long elaboratoId) {
        if (elaboratoId == null) {
            throw new InvalidElaboratoException();
        }
        if (!elaboratoRepository.existsById(elaboratoId)) {
            throw new ElaboratoNotFoundException();
        }
    }

    //rilancia ConsegnaNotValidException e ConsegnaNotFoundException
    private void checkConsegnaId(Long consegnaId) {
        if (consegnaId == null) {
            throw new InvalidConsegnaException();
        }
        if (!consegnaRepository.existsById(consegnaId)) {
            throw new ConsegnaNotFoundException();
        }
    }


    private void checkVmId(Long vmId) {
        if (vmId == null) {
            throw new InvalidVirtualMachineException();
        }
        if (!vmRepository.existsById(vmId)) {
            throw new VirtualMachineNotFoundException();
        }
    }


    @Override
    public Optional<CourseDTO> getCourse(String name) {
        if (name == null) {
            return Optional.empty();
        }
        if (!courseRepository.existsById(name)) {
            return Optional.empty();
        }
        return courseRepository.findById(name).map(c -> modelMapper.map(c, CourseDTO.class));
    }

    @Override
    public Optional<StudentDTO> getStudent(String studentId) {
        if (studentId == null) {
            return Optional.empty();
        }
        if (!studentRepository.existsById(studentId)) {
            return Optional.empty();
        }
        return studentRepository.findById(studentId).map(s -> modelMapper.map(s, StudentDTO.class));
    }

    @Override
    public boolean addCourse(CourseDTO courseDTO, ModelloVMDTO modelloVMDTO) {
        /*
         * controllo che i dati che mi arrivano siano validi
         * */
        if (courseDTO == null) {
            throw new InvalidCourseException();
        }
        if (modelloVMDTO == null) {
            throw new InvalidModelException();
        }
        if(modelloVMDTO.getMaxActiveVM() > modelloVMDTO.getMaxTotalVM()){
            throw new InvalidModelException();
        }
        if (courseDTO.getName() == null) {
            throw new InvalidCourseNameException();
        }
        if (courseDTO.getMin() > courseDTO.getMax()) {
            throw new InvalidCourseException();
        }
        if (courseRepository.existsById(courseDTO.getName())) {
            /*
             * se esiste già un corso con quel nome non posso aggiungerne un altro quindi ritorno false
             * */
            return false;
        } else {
            /*
             * controllo che chi ha fatto questa richiesta sia un docente
             * */
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!teacherRepository.existsById(userDetails.getUsername())) {
                throw new TeacherNotFoundException();
            }
            Course course = modelMapper.map(courseDTO, Course.class);
            ModelloVM modelloVM = modelMapper.map(modelloVMDTO, ModelloVM.class);
            course.setModelloVM(modelloVM);
            course.addTeacher(teacherRepository.getOne(userDetails.getUsername()));
            courseRepository.save(course);
            return true;
        }
    }

    @Override
    public boolean addStudent(StudentDTO student) {
        /*
         * controllo che i dati che mi arrivano siano validi
         * */


        if (student == null) {
            throw new InvalidStudentException();
        }
        if (student.getId() == null) {
            throw new InvalidStudentIdException();
        }
        if (student.getFirstName() == null) {
            throw new InvalidStudentException();
        }
        if (student.getName() == null) {
            throw new InvalidStudentException();
        }

        if (student.getEmail() == null) {
            throw new InvalidStudentException();
        }

        if (student.getPassword() == null) {
            throw new InvalidStudentException();
        }
        if (studentRepository.existsById(student.getId())) {
            /*
             * se esiste già uno studente con quell'id non posso aggiungerne un altro quindi ritorno false
             * */
            return false;
        } else {
            try {
                /*aggiungo l'utente agli users*/
                userDetailsServiceImpl.addUser(new UserDTO(student.getId(), "ROLE_STUDENT", student.getEmail(), student.getPassword()));
            } catch (InvalidUserException e) {
                return false; /*non voglio che lo stesso id venga dato ad uno studente e ad un docente*/
            }
            /*aggiungo l'utente agli studenti*/
            studentRepository.save(Student.builder()
                    .id(student.getId())
                    .name(student.getName())
                    .firstName(student.getFirstName())
                    .build());

            return true;
        }
    }

    @Override
    public boolean addTeacher(TeacherDTO teacher) {
        /*
         * controllo che i dati che mi arrivano siano validi
         * */
        if (teacher == null) {
            throw new InvalidTeacherException();
        }
        if (teacher.getId() == null) {
            throw new InvalidTeacherIdException();
        }
        if (teacher.getFirstName() == null) {
            throw new InvalidTeacherException();
        }
        if (teacher.getName() == null) {
            throw new InvalidTeacherException();
        }

        if (teacher.getEmail() == null) {
            throw new InvalidTeacherException();
        }

        if (teacher.getPassword() == null) {
            throw new InvalidTeacherException();
        }
        if (teacherRepository.existsById(teacher.getId())) {
            /*
             * se esiste già un docente con quell'id non posso aggiungerne un altro quindi ritorno false
             * */
            return false;
        } else {
            try {
                /*aggiungo l'utente agli users*/
                userDetailsServiceImpl.addUser(new UserDTO(teacher.getId(), "ROLE_TEACHER", teacher.getEmail(), teacher.getPassword()));
            } catch (InvalidUserException e) {
                return false; /*non voglio che lo stesso id venga dato ad uno studente e ad un docente*/
            }
            /*aggiungo l'utente ai docenti */
            teacherRepository.save(Teacher.builder()
                    .id(teacher.getId())
                    .name(teacher.getName())
                    .firstName(teacher.getFirstName())
                    .build());

            return true;
        }
    }

    @Override
    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll().stream().map(c -> modelMapper.map(c, CourseDTO.class)).collect(Collectors.toList());
    }

    @Override
    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll().stream().map(s -> modelMapper.map(s, StudentDTO.class)).collect(Collectors.toList());

    }

    @Override
    public List<StudentDTO> getEnrolledStudents(String courseName) {
        checkCourseName(courseName);

        return studentRepository
                .getByCoursesContaining(courseRepository.getOne(courseName))
                .stream()
                .map(s -> modelMapper.map(s, StudentDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentWithTeamInfoDTO> getEnrolledStudentsWithTeamInfo(String courseName) {
        checkCourseName(courseName);

        /*aggiungo ad ogni studente l'informazinoe su quale sia il team (se esiste) di questo corso di cui fa parte*/

        return studentRepository
                .getByCoursesContaining(courseRepository.getOne(courseName))
                .stream()
                .map(s -> {
                    String teamName;
                    List<Team> teams = s.getTeams();
                    if (teams.isEmpty()) {
                        teamName = "-";
                    } else {
                        Optional<String> opTeamName = teams.stream()
                                .filter(t -> t.getCourse().getName().equals(courseName))
                                .map(Team::getName)
                                .findFirst();
                        teamName = opTeamName.orElse("-");
                    }
                    return StudentWithTeamInfoDTO.builder().id(s.getId())
                            .firstName(s.getFirstName())
                            .name(s.getName())
                            .email("s" + s.getId() + "@studenti.polito.it")
                            .teamNameForSelectedCourse(teamName)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseDTO> getCourses(String studentId) {
        checkStudentId(studentId);


        return courseRepository
                .getByStudentsContaining(studentRepository.getOne(studentId))
                .stream()
                .map(c -> modelMapper.map(c, CourseDTO.class))
                .collect(Collectors.toList());

    }

    @Override
    public boolean addStudentToCourse(String studentId, String courseName) {
        checkCourseName(courseName);
        checkStudentId(studentId);

        Course course = courseRepository.getOne(courseName);
        if (!course.isEnabled()) {
            throw new CourseDisabledException();
        }

        Student student = studentRepository.getOne(studentId);
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        boolean trovato = false;

        /*controllo che il docente che ha fatto la richiesta sia tra i titolari del corso*/
        for (Teacher teacher : course.getTeachers()) {
            if (teacher.getId().equals(userDetails.getUsername())) {
                trovato = true;
            }
        }

        if (!trovato) throw new InvalidTeacherException();
        if (!courseRepository.existsByNameAndStudentsContaining(courseName, student)) {
            /*se non è già iscritto lo iscrivo e ritorno true*/
            course.addStudent(student);
            return true;
        } else {
            return false;
        }


    }

    @Override
    public void enableCourse(String courseName) {
        checkCourseName(courseName);
        Course course = courseRepository.getOne(courseName);
        course.setEnabled(true); /*se è gia enabled lo risetto a true*/

    }

    @Override
    public void disableCourse(String courseName) {
        checkCourseName(courseName);
        Course course = courseRepository.getOne(courseName);
        course.setEnabled(false); /*se è gia disabled lo risetto a false*/

    }


    @Override
    public List<Boolean> enrollAll(List<String> studentIds, String courseName) {
        /* il metodo this.addStudentToCourse(...) lancia eccezioni se i parametri sono null o non esistono nel db
         * sfrutto questo fatto per non ripetere tale controllo qui
         * */
        if (studentIds == null) {
            throw new InvalidStudentException();
        }
        List<Boolean> res = new ArrayList<>();
        for (String s : studentIds) {
            res.add(addStudentToCourse(s, courseName));
        }
        return res;
    }

    @Override
    public List<Boolean> enrollAll(Reader r, String courseName) {

        if (r == null) {
            throw new InvalidReaderException();
        }
        /*
         * il metodo enrollAll fa già i controlli opportuni lanciando eccezioni se sono violati
         * sfrutto questo fatto per non ripetere tali controlli qui
         * */

        try {
            CsvToBean<StudentDTO> csvToBean = new CsvToBeanBuilder<StudentDTO>(r)
                    .withType(StudentDTO.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<StudentDTO> students = csvToBean.parse();

            List<String> studentIds = new ArrayList<>();


            for (StudentDTO studentDTO : students) {
                Student s = studentRepository.getOne(studentDTO.getId());
                if (!s.getName().equals(studentDTO.getName()) || !s.getFirstName().equals(studentDTO.getFirstName()))
                    throw new InvalidStudentException();
                studentIds.add(studentDTO.getId());
            }
            return enrollAll(studentIds, courseName);
        } catch (InvalidStudentException e) {
            throw new InvalidStudentException();
        }

    }


    @Override
    public List<TeamDTO> getTeamsForStudent(String studentId) {
        checkStudentId(studentId);
        Student student = studentRepository.getOne(studentId);
        return teamRepository.getByMembersContaining(student)
                .stream()
                .map(t -> modelMapper.map(t, TeamDTO.class))
                .collect(Collectors.toList());

    }

    @Override
    public List<TeamDTO> getTeamsForCourse(String courseName) {
        checkCourseName(courseName);
        Course course = courseRepository.getOne(courseName);
        return teamRepository.getAllByCourseEquals(course)
                .stream()
                .map(t -> modelMapper.map(t, TeamDTO.class))
                .collect(Collectors.toList());

    }

    @Override
    public List<StudentDTO> getStudentsInTeams(String courseName) {
        checkCourseName(courseName);
        return courseRepository.getStudentsInTeams(courseName)
                .stream()
                .map(s -> modelMapper.map(s, StudentDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentDTO> getAvailableStudents(String courseName) {
        checkCourseName(courseName);
        Course course = courseRepository.getOne(courseName);

        /*prendo gli studenti che fanno già parte di un team attivo*/
        List<Student> students = course.getTeams().stream()
                .filter(team -> team.getStatus() == 1)
                .flatMap(t -> t.getMembers().stream()).collect(Collectors.toList());

        /*ritorno la differenza tra tutti gli iscritti al corso e gli studenti presi sopra*/
        return course.getStudents()
                .stream()
                .filter(s -> !students.contains(s))
                .map(s -> modelMapper.map(s, StudentDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentDTO> getMembers(Long teamId) {
        checkTeamId(teamId);

        Team team = teamRepository.getOne(teamId);

        return team.getMembers()
                .stream()
                .map(s -> modelMapper.map(s, StudentDTO.class))
                .collect(Collectors.toList());

    }


    @Override
    public TeamDTO proposeTeam(String courseName, String name, List<String> memberIds, Timestamp expiryDate) {
        if (memberIds == null) {
            throw new StudentNotFoundException();
        }
        if (name == null) {
            throw new InvalidTeamNameException();
        }

        checkCourseName(courseName);
        Course course = courseRepository.getOne(courseName);

        /*controllo che il nome del team proposto non sia già utilizzato in quel corso.*/
        List<Team> teamsUsingName = teamRepository.findAllByCourseAndName(course, name);
        if (!teamsUsingName.isEmpty()) {
            throw new InvalidTeamNameException();
        }

        /*controllo che gli studenti di cui mi sono dati gli id esistano nel db*/
        for (String studentId : memberIds) {
            if (studentId == null) {
                throw new StudentNotFoundException();
            } else if (!studentRepository.existsById(studentId)) {
                throw new StudentNotFoundException();
            }
        }

        /*controllo che non ci siano studenti duplicati*/
        if (memberIds.stream().distinct().count() != memberIds.size()) {
            /*c'è almeno un duplicato*/
            throw new DuplicateMemberException();
        }

        /*controllo che il corso sia abilitato*/
        if (!course.isEnabled()) {
            throw new CourseDisabledException();
        }

        /*controllo vincoli cardinalità corso faccio due controlli separati per poter lanciare eccezioni specifiche*/
        if (memberIds.size() < course.getMin()) {
            throw new NotEnoughStudentsException();
        }
        if (memberIds.size() > course.getMax()) {
            throw new TooManyStudentsException();
        }


        /*controllo che ogni studente di members sia iscritto al corso courseId*/
        List<String> enrolledIds = this.getEnrolledStudents(courseName)
                .stream()
                .map(StudentDTO::getId)
                .collect(Collectors.toList());

        for (String studentId : memberIds) {
            if (!enrolledIds.contains(studentId)) {
                throw new StudentNotEnrolledException();
            }
        }


        /*controllo che ogni members non faccia già parte di un altro team del corso*/
        List<Long> existingTeamsIds = course.getTeams().stream().map(Team::getId).collect(Collectors.toList());

        List<String> alreadyInATeam = teamRepository.findAllById(existingTeamsIds).stream()
                .filter(t -> t.getStatus() == 1)
                .map(Team::getMembers)
                .flatMap(Collection::stream)
                .distinct()
                .map(Student::getId)
                .collect(Collectors.toList());

        for (String studentId : memberIds) {
            if (alreadyInATeam.contains(studentId)) {
                throw new StudentAlreadyInATeamException();
            }
        }

        boolean trovato = false;
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        for (String sId : memberIds) {
            if (sId.equals(userDetails.getUsername())) {
                trovato = true;
            }
        }
        if (!trovato) {
            //lo studente che ha proposto il team non fa parte del team: non deve capitare
            throw new ForbiddenOperationException();
        }

        /*se sono qui tutti i vincoli sono rispettati*/


        List<Student> students = studentRepository.findAllById(memberIds);

        Team teamWithoutId = new Team();
        teamWithoutId.setCourse(course);
        teamWithoutId.setName(name);
        teamWithoutId.setStatus(0);
        teamWithoutId.setProponenteId(userDetails.getUsername());
        teamWithoutId.setMembers(students);
        Team teamWithId = teamRepository.save(teamWithoutId);
        TeamDTO teamDTO = modelMapper.map(teamWithId, TeamDTO.class);
        /*
        Lo studente che ha proposto il Team, automaticamente aderisce al Team quindi non lo metto in quelli a cui deve essere assciata una proposta per questo team
         */
        List<Student> proposte = students.stream()
                .filter(s -> !s.getId().equals(userDetails.getUsername())).collect(Collectors.toList());
        notificationService.notifyTeam(teamDTO, proposte, expiryDate);

        return teamDTO;

    }

    @Override
    public void activateTeam(Long teamId) {
        checkTeamId(teamId);
        Team team = teamRepository.getOne(teamId);
        team.setStatus(1);
    }

    @Override
    public void evictTeam(Long teamId) {
        checkTeamId(teamId);
        Team team = teamRepository.getOne(teamId);
        teamRepository.delete(team);
    }

    @Override
    public void evictTeams(List<Long> teamIds) {
        if (teamIds == null) {
            return;
        }
        /*controllo che i teams di cui mi sono dati gli id esistano nel db*/
        for (Long teamId : teamIds) {
            if (teamId == null) {
                teamIds.remove(teamId);
            } else if (!teamRepository.existsById(teamId)) {
                teamIds.remove(teamId);
            }
        }
        teamRepository.deleteAll(teamRepository.findAllById(teamIds));
    }


    @Override
    public Optional<TeacherDTO> getTeacher(String teacherId) {
        if (teacherId == null) {
            return Optional.empty();
        }
        if (!teacherRepository.existsById(teacherId)) {
            return Optional.empty();
        }
        return teacherRepository.findById(teacherId).map(t -> modelMapper.map(t, TeacherDTO.class));
    }

    @Override
    public List<TeacherDTO> getAllTeachers() {
        return teacherRepository.findAll().stream().map(t -> modelMapper.map(t, TeacherDTO.class)).collect(Collectors.toList());
    }


    @Override
    public void addTeacherForCourse(String teacherId, String courseName) {
        checkCourseName(courseName);
        checkTeacherId(teacherId);

        Course course = courseRepository.getOne(courseName);
        Teacher teacher = teacherRepository.getOne(teacherId);
        course.addTeacher(teacher);
    }

    @Override
    public List<TeacherDTO> getTeachersForCourse(String courseName) {
        checkCourseName(courseName);
        List<Teacher> teachers = courseRepository.getOne(courseName).getTeachers();
        if (teachers.isEmpty()) {
            return new ArrayList<TeacherDTO>();
        }
        return teachers.stream().map(t -> modelMapper.map(t, TeacherDTO.class)).collect(Collectors.toList());
    }

    @Override

    public List<CourseDTO> getCoursesForTeacher(String teacherId) {
        checkTeacherId(teacherId);
        return teacherRepository.findById(teacherId).get().getCourses().stream()
                .map(c -> modelMapper.map(c, CourseDTO.class))
                .collect(Collectors.toList());
    }


    @Override
    public void deleteCourse(String courseName) {

        checkCourseName(courseName);
        Course course = courseRepository.getOne(courseName);
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        /*controllo che chi fa la richiesta sia docente titolare del corso*/
        boolean trovato = false;

        for (Teacher teacher : course.getTeachers()) {
            if (teacher.getId().equals(userDetails.getUsername())) {
                trovato = true;
            }
        }

        if (!trovato) throw new InvalidTeacherException();


        List<Student> students = studentRepository.getByCoursesContaining(course);

        for (Student s : students) {
            course.removeStudent(s);
        }

        course.getConsegne().stream().flatMap(c -> c.getElaborati().stream()).forEach(el -> elaboratoRepository.delete(el));

        course.getConsegne().forEach(c -> consegnaRepository.delete(c));

        course.getTeams().forEach(t -> {
                    t.getVirtualMachines().forEach(vm -> vmRepository.delete(vm));
                    teamRepository.delete(t);
                }
        );
        modelloVMRepository.delete(course.getModelloVM());
        courseRepository.delete(course);
    }

    @Override
    public void updateCourse(CourseDTO courseDTO) {

        if (courseDTO == null) {
            throw new CourseNotFoundException();
        }

        checkCourseName(courseDTO.getName());

        if (courseDTO.getMin() > courseDTO.getMax()) throw new InvalidCourseException();

        if (courseDTO.getMin() < 0 || courseDTO.getMax() < 0) throw new InvalidCourseException();


        Course course = courseRepository.getOne(courseDTO.getName());
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        /*controllo che chi fa la richiesta sia docente titolare del corso*/
        boolean trovato = false;

        for (Teacher teacher : course.getTeachers()) {
            if (teacher.getId().equals(userDetails.getUsername())) {
                trovato = true;
            }
        }

        if (!trovato) throw new InvalidTeacherException();

        course.setAcronimo(courseDTO.getAcronimo());
        course.setMin(courseDTO.getMin());
        course.setMax(courseDTO.getMax());
        course.setEnabled(courseDTO.isEnabled());

    }


    @Override
    public boolean removeStudentFromCourse(String studentId, String courseName) {

        checkCourseName(courseName);
        checkStudentId(studentId);

        Course course = courseRepository.getOne(courseName);
        if (!course.isEnabled()) {
            throw new CourseDisabledException();
        }

        Student student = studentRepository.getOne(studentId);

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        boolean trovato = false;
        /*controllo che il teacher loggato sia docente del corso in questione*/
        for (Teacher teacher : course.getTeachers()) {
            if (teacher.getId().equals(userDetails.getUsername())) {
                trovato = true;
            }
        }

        if (!trovato) throw new InvalidTeacherException();

        if (courseRepository.existsByNameAndStudentsContaining(courseName, student)) {
            /*prima di cancellare lo studente dal corso smantello il suo team e le vm a loro associate*/
            Optional<Team> teamOp = student.getTeams().stream().filter(t -> t.getCourse().getName().equals(course.getName())).findAny();
            if (teamOp.isPresent()) {
                Team team = teamOp.get();
                team.getVirtualMachines().stream()
                        .forEach(virtualMachine -> vmRepository.delete(virtualMachine));

                teamRepository.delete(team);
            }
            /*cancello tutti i suoi elaborati*/
            student.getElaborati().stream()
                    .filter(elaborato -> elaborato.getConsegna().getCourse().getName().equals(course.getName()))
                    .forEach(elaborato -> elaboratoRepository.delete(elaborato));

            course.removeStudent(student);
            return true;
        } else {
            return false;
        }

    }


    @Override
    public boolean updateModelloVM(String courseName, ModelloVMDTO modelloVMDTO) {

        if (modelloVMDTO == null) {
            throw new InvalidModelException();
        }
        checkCourseName(courseName);

        Course course = courseRepository.getOne(courseName);

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        /*controllo che chi fa la richiesta sia docente titolare del corso*/
        boolean trovato = false;

        for (Teacher teacher : course.getTeachers()) {
            if (teacher.getId().equals(userDetails.getUsername())) {
                trovato = true;
            }
        }

        if (!trovato) throw new InvalidUserException();

        if( modelloVMDTO.getDiskSpaceMB() < 0
                || modelloVMDTO.getNumVcpu() < 0
                || modelloVMDTO.getRamMB() < 0
                || modelloVMDTO.getMaxActiveVM() < 0
                || modelloVMDTO.getMaxTotalVM() < 1
                || modelloVMDTO.getMaxActiveVM() > modelloVMDTO.getMaxTotalVM()){
            throw new InvalidModelException();
        }

        /*controllo se è possibile fare l'update del modello vm sulla base delle risorse attualmente in uso*/

        List<List<VirtualMachine>> list = course.getTeams().stream().map(Team::getVirtualMachines).collect(Collectors.toList());


        int counterVMAttive = 0;
        int counterVMTotale = 0;

        for (List<VirtualMachine> l : list) {
            counterVMTotale += l.size();
            /*accumulatore risorse attualemente in uso nel team corrente = iterazione corrente*/
            VirtualMachine virtualMachine = VirtualMachine.builder().diskSpaceMB(0).numVcpu(0).ramMB(0).build();
            for (VirtualMachine vm : l) {
                if (vm.isAttiva()) {
                    counterVMAttive++;
                }
                virtualMachine.setDiskSpaceMB(vm.getDiskSpaceMB() + virtualMachine.getDiskSpaceMB());
                virtualMachine.setNumVcpu(vm.getNumVcpu() + virtualMachine.getNumVcpu());
                virtualMachine.setRamMB(vm.getRamMB() + virtualMachine.getRamMB());
            }
            if (virtualMachine.getRamMB() > modelloVMDTO.getRamMB() || virtualMachine.getNumVcpu() > modelloVMDTO.getNumVcpu() || virtualMachine.getDiskSpaceMB() > modelloVMDTO.getDiskSpaceMB()) {
                throw new InvalidModelException();
            }
        }

        if (counterVMAttive > modelloVMDTO.getMaxActiveVM() || counterVMTotale > modelloVMDTO.getMaxTotalVM()) {
            throw new InvalidModelException();

        }


        ModelloVM modelloVM = course.getModelloVM();
        modelloVM.setDiskSpaceMB(modelloVMDTO.getDiskSpaceMB());
        modelloVM.setNumVcpu(modelloVMDTO.getNumVcpu());
        modelloVM.setRamMB(modelloVMDTO.getRamMB());
        modelloVM.setMaxActiveVM(modelloVMDTO.getMaxActiveVM());
        modelloVM.setMaxTotalVM(modelloVMDTO.getMaxTotalVM());

        return true;
    }

    @Override
    public VirtualMachineDTO createVM(VirtualMachineDTO virtualMachineDTO, String courseName, String studentId, Long teamId) {

        checkCourseName(courseName);
        checkStudentId(studentId);

        Course course = courseRepository.getOne(courseName);
        Student student = studentRepository.getOne(studentId);

        if (!course.getStudents().contains(student)) {
            throw new InvalidStudentException();
            //il corso non contiene lo studente
        }

        String sId = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();

        if (!sId.equals(studentId)) {
            throw new InvalidStudentException();
            //l'utente loggato che fa la richiesta è diverso dallo studentId o creatore VM e valore passato nel path non coincidono
        }

        checkTeamId(teamId);

        Team team = teamRepository.getOne(teamId);


        if (!team.getMembers().contains(student)) {
            throw new InvalidStudentException(); //lo studente non è nel team
        }

        List<VirtualMachine> virtualMachinesForTeam = team.getVirtualMachines();

        ModelloVM modelloVM = course.getModelloVM();

        /*controllo che sia possibile creare la vm richiesta sulla base delle risorse attualmente in uso dal team*/

        if (modelloVM.getMaxTotalVM() < virtualMachinesForTeam.size() + 1) {
            throw new InvalidModelException(); //il numero di virtual machine totali supera quelle del modello
        }

        Optional<Integer> ramOccupataMB = virtualMachinesForTeam.stream().map(VirtualMachine::getRamMB).reduce(Integer::sum);
        Optional<Integer> vCpuTot = virtualMachinesForTeam.stream().map(VirtualMachine::getNumVcpu).reduce(Integer::sum);
        Optional<Integer> diskSpaceMB = virtualMachinesForTeam.stream().map(VirtualMachine::getDiskSpaceMB).reduce(Integer::sum);

        int ramOccupataMBTOT = ramOccupataMB.orElse(0);
        int vCPUTOT = vCpuTot.orElse(0);
        int diskSpaceMBTOT = diskSpaceMB.orElse(0);


        if ((ramOccupataMBTOT + virtualMachineDTO.getRamMB()) > modelloVM.getRamMB()
                || (vCPUTOT + virtualMachineDTO.getNumVcpu()) > modelloVM.getNumVcpu()
                || (diskSpaceMBTOT + virtualMachineDTO.getDiskSpaceMB()) > modelloVM.getDiskSpaceMB()) {
            throw new InvalidModelException(); //
        }


        VirtualMachine vm = VirtualMachine.builder()
                .diskSpaceMB(virtualMachineDTO.getDiskSpaceMB())
                .ramMB(virtualMachineDTO.getRamMB())
                .numVcpu(virtualMachineDTO.getNumVcpu())
                .creator(studentId)
                .team(team)
                .owners(Collections.singletonList(student)).build();


        vmRepository.save(vm);


        return modelMapper.map(vm, VirtualMachineDTO.class);


    }

    @Override
    public List<PropostaTeamDTO> getTeamRequests(String courseName, String sId) {

        if (sId == null) {
            throw new StudentNotFoundException();
        }
        if (!studentRepository.existsById(sId)) {
            throw new StudentNotFoundException();
        }

        String studentId = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();

        if (!sId.equals(studentId)) {
            throw new InvalidStudentException();
        }

        if (courseName == null) {
            throw new CourseNotFoundException();
        }
        if (!courseRepository.findById(courseName).isPresent()) {
            throw new CourseNotFoundException();
        }

        Course course = courseRepository.findById(courseName).get();


        List<Token> tokens = tokenRepository.findAllByStudent(studentRepository.getOne(sId));

        List<PropostaTeamDTO> propostaTeamDTOS = new ArrayList<>();

        for (Token t : tokens) {

            /*per ogni token associato a questo studente creo l'oggetto propostaTeamDTO a questo associato
             * solo se il corso del team associato al token corrisponde al corso in questione
             * */

            PropostaTeamDTO propostaTeamDTO = new PropostaTeamDTO();
            if (!teamRepository.existsById(t.getTeamId())) {
                throw new TeamNotFoundException();
            }


            Team team = teamRepository.getOne(t.getTeamId());

            if (team.getCourse().getName().equals(courseName)) {


                TeamDTO teamDTO = modelMapper.map(team, TeamDTO.class);

                propostaTeamDTO.setTeamDTO(teamDTO);
                propostaTeamDTO.setToken(t.getId());

                List<StudentWithStatusDTO> studentWithStatusDTOS = new ArrayList<>();

                propostaTeamDTO.setStudentWithStatusDTOS(studentWithStatusDTOS);

                for (Student s : team.getMembers()) {

                    StudentWithStatusDTO studentWithStatusDTO = StudentWithStatusDTO.
                            builder().id(s.getId()).name(s.getName()).firstName(s.getFirstName()).build();

                    studentWithStatusDTOS.add(studentWithStatusDTO);
                /*
                 risultato dell'if è true se non ha ancora nè confermato nè rifiutato
                 */
                    if (tokenRepository.existsByStudentEqualsAndTeamIdEquals(s, team.getId())) {
                        studentWithStatusDTO.setStatus("In attesa di risposta");

                    } else {
                        studentWithStatusDTO.setStatus("Partecipazione Confermata");
                    }


                }
                propostaTeamDTOS.add(propostaTeamDTO);

            }

        }

        return propostaTeamDTOS;
    }

    @Override
    public void setImageForStudent(String studentId, Byte[] image) {
        checkStudentId(studentId);
        Student student = studentRepository.getOne(studentId);
        student.setImage(image);
    }

    @Override
    public Byte[] getImageFromStudent(String studentId) {
        checkStudentId(studentId);
        Student student = studentRepository.getOne(studentId);
        return student.getImage();
    }

    @Override
    public void setImageForTeacher(String teacherId, Byte[] image) {
        checkTeacherId(teacherId);
        Teacher teacher = teacherRepository.getOne(teacherId);
        teacher.setImage(image);
    }

    @Override
    public Byte[] getImageFromTeacher(String teacherId) {
        checkTeacherId(teacherId);
        Teacher teacher = teacherRepository.getOne(teacherId);
        return teacher.getImage();
    }

    @Override
    public void createConsegna(String scadenza, String courseName, String nomeConsegna, Byte[] contenuto) throws ParseException {
        checkCourseName(courseName);
        Course course = courseRepository.getOne(courseName);

        String teacherId = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        checkTeacherInCourse(teacherId, courseName); /*al suo interno chiama la checkTeacherId*/


        Instant instant = Instant.now();
        Timestamp rilascio = Timestamp.from(instant);

        DateFormat formatter;
        formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = formatter.parse(scadenza);

        int hoursToAdd;

        if (date.toString().split(" ")[3].compareTo("12:00:00") != 0) {
            hoursToAdd = 43200000 * 2 - 1;
        } else {
            hoursToAdd = 43199999;
        }

        Timestamp scadenzaTimestamp = new Timestamp(date.getTime() + hoursToAdd);


        if (scadenzaTimestamp.before(rilascio)) {
            throw new InvalidConsegnaException();
        }

        Consegna consegna = Consegna.builder()
                .scadenza(scadenzaTimestamp)
                .nomeConsegna(nomeConsegna)
                .rilascio(rilascio)
                .elaborati(new ArrayList<>())
                .contenuto(contenuto)
                .course(course)
                .build();
        consegnaRepository.save(consegna);

        // Creo un elaborato con stato NULL per ogni studente.
        List<Student> students = course.getStudents();
        Timestamp forElaborato = new Timestamp(System.currentTimeMillis());
        students.forEach(student -> {
            Elaborato elaborato = Elaborato.builder()
                    .consegna(consegna)
                    .dataCaricamento(forElaborato)
                    .stato("NULL")
                    .student(student)
                    .voto("-")
                    .possibileRiconsegna("-")
                    .build();
            elaboratoRepository.save(elaborato);
        });
    }

    @Override
    public Byte[] getConsegna(long consegnaId, String studentId) {

        checkConsegnaId(consegnaId);
        checkStudentId(studentId);

        checkAuthorizationId(studentId);


        Student student = studentRepository.getOne(studentId);
        Consegna consegna = consegnaRepository.getOne(consegnaId);

        List<Elaborato> listaElaborati = elaboratoRepository.findAllByConsegnaAndStudent(consegna, student);

        /*la prima volta che lo studente legge la consegna fa si che si crei un elaborato con stato LETTO*/
        if (listaElaborati.size() == 1 && listaElaborati.get(0).getStato().equals("NULL")) {
            Instant instant = Instant.now();
            Timestamp dataLettura = Timestamp.from(instant);

            Elaborato e = Elaborato.builder().stato("LETTO")
                    .student(student)
                    .consegna(consegna)
                    .contenuto(null)
                    .possibileRiconsegna("-")
                    .voto("-")
                    .dataCaricamento(dataLettura)
                    .build();

            elaboratoRepository.save(e);
        }


        return consegna.getContenuto();
    }

    @Override
    public Byte[] getElaboratoById(String elaboratoId, String studentId) {
        checkAuthorizationId(studentId);
        if (elaboratoRepository.existsById(Long.parseLong(elaboratoId))) {
            Elaborato e = elaboratoRepository.getOne(Long.parseLong(elaboratoId));
            return e.getContenuto();
        } else {
            throw new ElaboratoNotFoundException();
        }
    }

    @Override
    public List<StudentDTO> getAvailableStudentsForVM(String studentId, String courseName, Long teamId, Long vmId) {

        checkCourseName(courseName);

        checkAuthorizationId(studentId);

        Course course = courseRepository.getOne(courseName);

        checkStudentId(studentId);

        Student student = studentRepository.getOne(studentId);

        if (!course.getStudents().contains(student)) {
            throw new StudentNotEnrolledException();
        }

        if (!teamRepository.existsById(teamId)) {
            throw new TeamNotFoundException();
        }

        Team team = teamRepository.getOne(teamId);

        if (!team.getMembers().contains(student)) {
            throw new StudentNotInTeamException();
        }

        if (!vmRepository.existsById(vmId)) {
            throw new VirtualMachineNotFoundException();
        }
        VirtualMachine vm = vmRepository.getOne(vmId);

        if (!team.getVirtualMachines().contains(vm)) {
            throw new InvalidTeamException();
        }

        if (!vm.getOwners().contains(student)) {
            throw new StudentNotOwnerException();
        }

        return team.getMembers().stream().filter(s -> !vm.getOwners().contains(s)).map(s -> modelMapper.map(s, StudentDTO.class)).collect(Collectors.toList());

    }

    @Override
    public Boolean checkAcceptedRequest(String studentId, String courseName) {

        /*serve per sapere se di tutte le proposte che ha ricevuto per questo corso lo studente ne ha accettata una*/

        checkStudentId(studentId);
        Student student = studentRepository.getOne(studentId);

        checkCourseName(courseName);
        Course course = courseRepository.getOne(courseName);

        checkAuthorizationId(studentId);

        List<Team> teams = teamRepository.getAllByCourseEquals(course)
                .stream()
                .filter(t -> t.getMembers().contains(student))
                .filter(t -> t.getStatus() == 0)
                .collect(Collectors.toList());

        boolean accettato = false;
        int cont = 0;

        for (Team t : teams) {
            if (!tokenRepository.existsByStudentEqualsAndTeamIdEquals(student, t.getId())) {
                cont++;
                accettato = true;
            }
        }
        // Controllo aggiunto, dovrebbe entrare al più una volta
        if (cont > 1) {
            throw new InvalidTeamException();
        }


        return accettato;
    }

    @Override
    public byte[] getVM() throws IOException {
        //non facciamo controlli in quanto a tutti gli studenti restituiamo la stessa vm fittizia
        VirtualMachine vm = VirtualMachine.builder()
                .diskSpaceMB(0)
                .ramMB(0)
                .numVcpu(0)
                .creator("p")
                .build();

        return Base64.getEncoder().encode(ArrayUtils.toPrimitive(vm.getScreen()));
    }


    @Override
    public Byte[] leggiCorrezione(Long consegnaId, String studentId, String elabId) {

        checkConsegnaId(consegnaId);

        checkStudentId(studentId);

        checkAuthorizationId(studentId);

        if (!elaboratoRepository.existsById(Long.parseLong(elabId))) {
            throw new ElaboratoNotFoundException();
        }

        Student student = studentRepository.getOne(studentId);
        Consegna consegna = consegnaRepository.getOne(consegnaId);

        List<Elaborato> elaboratiOrdinati = elaboratoRepository.findAllByConsegnaAndStudent(consegna, student)
                .stream()
                .sorted(Comparator.comparing(Elaborato::getDataCaricamento).reversed())
                .collect(Collectors.toList());
        Elaborato correzione = elaboratiOrdinati.get(1);
        Elaborato elaborato = elaboratiOrdinati.get(0);

        /*se la variabile correzione è una correzione e se è la correzione su cui ha cliccato l'utente e se dopo c'è un elaborato con stato NULL*/
        if (correzione.getStato().equals("RIVISTO") && correzione.getId() == Long.parseLong(elabId) && elaborato.getStato().equals("NULL")) {

            /*allora creo un elaborato con stato LETTO perchè è la prima volta che lo studente legge questa correzione*/
            Instant instant = Instant.now();
            Timestamp dataLettura = Timestamp.from(instant);

            Elaborato e = Elaborato.builder().stato("LETTO")
                    .student(student)
                    .consegna(consegna)
                    .possibileRiconsegna("-")
                    .voto("-")
                    .dataCaricamento(dataLettura)
                    .build();

            elaboratoRepository.save(e);


        }

        return elaboratoRepository.getOne(Long.parseLong(elabId)).getContenuto();

    }

    @Override
    public ModelloVMDTO getModelloVM(String courseName) {
        checkCourseName(courseName);
        Course course = courseRepository.getOne(courseName);
        return modelMapper.map(course.getModelloVM(), ModelloVMDTO.class);
    }

    @Override
    public List<StudentDTO> getNotEnrolledStudents(String courseName) {
        checkCourseName(courseName);


        return studentRepository
                .getByCoursesNotContaining(courseRepository.getOne(courseName))
                .stream()
                .map(s -> modelMapper.map(s, StudentDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public TeamDTO getTeamForStudentAndCourse(String studentId, String courseName) {

        checkCourseName(courseName);
        checkStudentId(studentId);
        checkAuthorizationId(studentId);

        Student student = studentRepository.getOne(studentId);

        Optional<Team> team = student.getTeams().stream().filter(t -> t.getCourse().getName().equals(courseName)).findFirst();

        return team.map(value -> modelMapper.map(value, TeamDTO.class)).orElse(null);
    }

    @Override
    public List<Long> getOwnedVM(String studentId, String courseName, Long teamId) {

        checkCourseName(courseName);
        checkStudentId(studentId);
        checkAuthorizationId(studentId);

        if (!teamRepository.existsById(teamId)) {
            throw new TeamNotFoundException();
        }

        Team team = teamRepository.getOne(teamId);
        Student student = studentRepository.getOne(studentId);

        return team.getVirtualMachines()
                .stream()
                .filter(v -> v.getOwners().contains(student))
                .map(VirtualMachine::getId)
                .collect(Collectors.toList());
    }

    @Override
    public List<ElaboratoDTO> getElaboratiForStudent(String studentId, Long consegnaId) {

        checkStudentId(studentId);

        Student student = studentRepository.getOne(studentId);

        checkConsegnaId(consegnaId);

        Consegna consegna = consegnaRepository.getOne(consegnaId);

        return elaboratoRepository.findAllByConsegnaAndStudent(consegna, student)
                .stream()
                .map(s -> modelMapper.map(s, ElaboratoDTO.class))
                .collect(Collectors.toList());


    }

    @Override
    public boolean consegnaElaborato(Long consegnaId, String studentId, Byte[] contenuto) {

        checkConsegnaId(consegnaId);
        checkStudentId(studentId);
        checkAuthorizationId(studentId);

        Instant instant = Instant.now();
        Timestamp dataConsegna = Timestamp.from(instant);

        Student student = studentRepository.getOne(studentId);
        Consegna consegna = consegnaRepository.getOne(consegnaId);

        if (consegna.getScadenza().before(dataConsegna)) {
            throw new InvalidElaboratoException();
        }

        List<Elaborato> elaborati = elaboratoRepository.findAllByConsegnaAndStudent(consegna, student);

        List<Elaborato> elaboratiOrdinati = elaborati.stream().
                sorted(Comparator.comparing(Elaborato::getDataCaricamento).reversed())
                .collect(Collectors.toList());
        Elaborato ultimoElaborato = elaboratiOrdinati.get(0);

        //controllo se l'ultimo Elaborato ha stato LETTO
        //Nel caso in cui uno provi a consegnare senza aver letto o
        // avendo già consegnato viene scatenata questa eccezione
        if (!ultimoElaborato.getStato().equals("LETTO")) {
            throw new ElaboratoNotFoundException();
        }

        Elaborato e = Elaborato.builder().stato("CONSEGNATO")
                .student(student)
                .consegna(consegna)
                .contenuto(contenuto)
                .possibileRiconsegna("-")
                .voto("-")
                .dataCaricamento(dataConsegna)
                .build();

        elaboratoRepository.save(e);

        return true;

    }

    @Override
    public void correggereElaborato(Long elaboratoId, String courseName,
                                    Long consegnaId, Byte[] contenuto,
                                    String possibileRiconsegna, String voto) {

        checkCourseName(courseName);

        checkElaboratoId(elaboratoId);

        Elaborato elaborato = elaboratoRepository.getOne(elaboratoId);
        if (!elaborato.getStato().equals("CONSEGNATO")) {
            throw new InvalidElaboratoException();
        }


        if (!courseName.equals(elaborato.getConsegna().getCourse().getName())) {
            throw new InvalidCourseException(); //il corso indicato nel path non coincide con quello associato all'elaborato.
        }


        checkConsegnaId(consegnaId);

        if (!consegnaId.equals(elaborato.getConsegna().getId())) {
            throw new InvalidConsegnaException(); //la consegnaId indicata nel path non coincide con quello associato all'elaborato.
        }

        Consegna consegna = consegnaRepository.getOne(consegnaId);

        String studentId = elaborato.getStudent().getId();

        checkStudentId(studentId);

        Student student = studentRepository.getOne(studentId);

        //Controllo che il voto esista solo se non è possibile riconsegnare.
        if ((possibileRiconsegna.equals("true") && !voto.equals("-")) || (possibileRiconsegna.equals("false") && voto.equals("-"))) {
            throw new InvalidVotoException();
        }


        //Controllo se chi sta revisionando è un professore titolare del corso
        String teacherId = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        checkTeacherInCourse(teacherId, courseName);

        //cerco l'ultimo elaborato caricato che sia associato a questo studente e a questa consegna.
        //La correzione può essere fatta solo se l'ultimo elaborato caricato ha stato CONSEGNATO
        List<Elaborato> elaborati = elaboratoRepository.findAllByConsegnaAndStudent(elaborato.getConsegna(), elaborato.getStudent());

        List<Elaborato> elaboratiOrdinati = elaborati.stream().
                sorted(Comparator.comparing(Elaborato::getDataCaricamento).reversed())
                .collect(Collectors.toList());
        Elaborato ultimoElaborato = elaboratiOrdinati.get(0);

        //controllo se l'ultimo Elaborato ha stato CONSEGNATO

        if (!ultimoElaborato.getStato().equals("CONSEGNATO")) {
            throw new InvalidElaboratoException();
        }


        Elaborato revisione = Elaborato.builder()
                .dataCaricamento(Timestamp.from(Instant.now()))
                .contenuto(contenuto)
                .consegna(ultimoElaborato.getConsegna())
                .student(ultimoElaborato.getStudent())
                .possibileRiconsegna(possibileRiconsegna)
                .voto(voto)
                .stato("RIVISTO").build();

        elaboratoRepository.save(revisione);

        /*
         Ora nel caso sia possibile una riconsegna creo un nuovo elaborato con stato NULL. Quando lo studente leggerà
         la correzione verrà creato un elaborato con stato LETTO. Solo allora lo studente potrà consegnare un nuovo elaborato.
         */
        if (possibileRiconsegna.equals("true")) {
            Elaborato elaboratoNull = Elaborato.builder()
                    .consegna(consegna)
                    .stato("NULL")
                    .student(student)
                    .dataCaricamento(Timestamp.from(Instant.now().plus(Long.parseLong("2"), ChronoUnit.SECONDS)))
                    .possibileRiconsegna("-")
                    .voto("-")
                    .build();
            elaboratoRepository.save(elaboratoNull);
        }

    }

    @Override
    public List<ConsegnaDTO> getConsegne(String studentId, String courseName) {

        //rilancia InvalidCourseException e CourseNotFoundException
        checkCourseName(courseName);

        //rilancia InvalidStudentException e StudentNotFoundException
        checkStudentId(studentId);

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String studentUsername = userDetails.getUsername();

        if (!studentUsername.equals(studentId)) {
            throw new InvalidUserException();
        }

        List<Consegna> consegne = consegnaRepository.getConsegneByStudentAndCourse(studentId, courseName);

        return consegne.stream()
                .map(c -> modelMapper.map(c, ConsegnaDTO.class)).collect(Collectors.toList());

    }

    @Override
    public List<ConsegnaDTO> getConsegneForCourse(String courseName) {

        checkCourseName(courseName);

        Course course = courseRepository.getOne(courseName);
        List<Consegna> consegne = consegnaRepository.getConsegnasByCourse(course);

        return consegne.stream()
                .map(c -> modelMapper.map(c, ConsegnaDTO.class)).collect(Collectors.toList());

    }

    @Override
    public Byte[] getConsegnaForCourse(String consegnaId) {
        checkConsegnaId(Long.parseLong(consegnaId));
        Consegna c = consegnaRepository.getOne(Long.parseLong(consegnaId));
        return c.getContenuto();

    }

    @Override
    public List<ElaboratoForTeacherDTO> getElaborati(String courseName, Long consegnaId) {

        checkCourseName(courseName);

        checkConsegnaId(consegnaId);

        //Controllo se chi sta ottenendo gli elaborati è un professore titolare del corso
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String teacherId = userDetails.getUsername();
        checkTeacherInCourse(teacherId, courseName);

        List<Elaborato> elaborati = elaboratoRepository.getElaboratiByConsegnaAndCourse(consegnaId, courseName);

        List<StudentDTO> studentiIscritti = getEnrolledStudents(courseName);
        List<ElaboratoForTeacherDTO> elaboratiForTeacher = new ArrayList<>();

        /*
         * per ogni elaborato creo il corrispondente elaboratoForTeacher che in più ha le informazioni sullo studente
         * */

        for (Elaborato elaborato : elaborati) {

            StudentDTO student = null;
            for (StudentDTO s : studentiIscritti) {
                if (s.getId().equals(elaborato.getStudent().getId())) {
                    student = s;
                    break;
                }
            }
            assert student != null;
            ElaboratoForTeacherDTO elaboratoForTeacherDTO = ElaboratoForTeacherDTO.builder()
                    .id(elaborato.getId())
                    .studentId(student.getId())
                    .name(student.getName())
                    .firstName(student.getFirstName())
                    .stato(elaborato.getStato())
                    .possibileRiconsegna(elaborato.getPossibileRiconsegna())
                    .voto(elaborato.getVoto())
                    .dataCaricamento(elaborato.getDataCaricamento())
                    .build();

            elaboratiForTeacher.add(elaboratoForTeacherDTO);
        }

        return elaboratiForTeacher;
    }

    @Override
    public List<ElaboratoDTO> getUltimiElaborati(String courseName, Long consegnaId) {

        /*questo metoo serve per ottenere l'ultimo (cronologicamente) elaborato di ogni studente per una consegna di un corso*/

        checkCourseName(courseName);


        checkConsegnaId(consegnaId);

        //Controllo se chi sta ottenendo gli elaborati è un professore titolare del corso
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String teacherId = userDetails.getUsername();
        checkTeacherInCourse(teacherId, courseName);

        List<Elaborato> elaborati = elaboratoRepository.getElaboratiByConsegnaAndCourse(consegnaId, courseName);

        /*raggruppo gli elaborati di ogni studente*/
        Map<String, List<Elaborato>> map = elaborati.stream().collect(Collectors.groupingBy(e -> e.getStudent().getId()));

        /*ordino per data di caricamento gli elaborati di ogni studente*/
        for (String id : map.keySet()) {
            map.get(id).sort(Comparator.comparing(Elaborato::getDataCaricamento).reversed());
        }

        /*per ogni studente prendo solo l'ultimo elaborato*/
        List<Elaborato> result = new ArrayList<>();
        for (String id : map.keySet()) {
            result.add(map.get(id).get(0));
        }
        return result.stream()
                .map(e -> modelMapper.map(e, ElaboratoDTO.class)).collect(Collectors.toList());
    }

    @Override
    public Byte[] getElaborato(Long elaboratoId, String courseName, String studentId, Long consegnaId) {


        checkElaboratoId(elaboratoId);

        Elaborato elaborato = elaboratoRepository.getOne(elaboratoId);

        //controllo che il corso nell'url sia giusto
        if (courseName == null || !courseName.equals(elaborato.getConsegna().getCourse().getName())) {
            throw new InvalidCourseException();
        }
        //controllo che lo studentId nell'url sia giusto
        if (studentId == null || !studentId.equals(elaborato.getStudent().getId())) {
            throw new InvalidStudentException();
        }
        //controllo che la consegnaId nell'url sia giusto
        if (consegnaId == null || !consegnaId.equals(elaborato.getConsegna().getId())) {
            throw new InvalidConsegnaException();
        }

        //Controllo se chi ha chiesto l'elaborato è un professore titolare del corso
        String teacherId = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        checkTeacherInCourse(teacherId, courseName);

        return elaborato.getContenuto();

    }

    @Override
    public List<ElaboratoDTO> getElaboratiConsegnatiFromStudentId(String courseName, Long consegnaId, String studentId) {

        checkCourseName(courseName);

        checkConsegnaId(consegnaId);

        checkStudentId(studentId);

        //Controllo se chi sta ottenendo gli elaborati è un professore titolare del corso
        String teacherId = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        checkTeacherInCourse(teacherId, courseName);

        List<Elaborato> elaborati = elaboratoRepository.getElaboratiByConsegnaAndCourseAndStudentId(consegnaId, courseName, studentId);

        return elaborati.stream()
                .filter(e -> e.getStato().equals("CONSEGNATO"))
                .sorted(Comparator.comparing(Elaborato::getDataCaricamento).reversed())
                .map(e -> modelMapper.map(e, ElaboratoDTO.class)).collect(Collectors.toList());
    }


    @Override
    public VirtualMachineDTO manageVM(VirtualMachineDTO virtualMachineDTO, Long virtualMachineId, String studentId, String courseName) {

        checkVmId(virtualMachineId);

        VirtualMachine virtualMachine = vmRepository.getOne(virtualMachineId);

        checkCourseName(courseName);
        checkStudentId(studentId);

        checkAuthorizationId(studentId);

        Student student = studentRepository.getOne(studentId);
        Course course = courseRepository.getOne(courseName);

        if (!virtualMachine.getOwners().contains(student)) {
            throw new InvalidStudentException(); //lo studente se non è owner della vm non la può attivare
        }

        Team team = virtualMachine.getTeam();

        if (!teamRepository.existsById(team.getId())) {
            throw new TeamNotFoundException(); //non deve mai entrare in questa eccezione
        }

        if (!team.getMembers().contains(student)) {
            throw new InvalidStudentException(); //lo studente non fa parte del team
        }

        List<VirtualMachine> virtualMachinesForTeam = team.getVirtualMachines();

        ModelloVM modelloVM = course.getModelloVM();

        Optional<Integer> ramOccupataMB = virtualMachinesForTeam.stream().map(VirtualMachine::getRamMB).reduce(Integer::sum);
        Optional<Integer> vCpuTot = virtualMachinesForTeam.stream().map(VirtualMachine::getNumVcpu).reduce(Integer::sum);
        Optional<Integer> diskSpaceMB = virtualMachinesForTeam.stream().map(VirtualMachine::getDiskSpaceMB).reduce(Integer::sum);

        int ramOccupataMBTOT = ramOccupataMB.orElse(0);
        int vCPUTOT = vCpuTot.orElse(0);
        int diskSpaceMBTOT = diskSpaceMB.orElse(0);


        /*controllo se è possibile fare l'update della vm sulla base delle risorse attualmente in uso*/
        if ((ramOccupataMBTOT + virtualMachineDTO.getRamMB() - virtualMachine.getRamMB()) > modelloVM.getRamMB()
                || (vCPUTOT + virtualMachineDTO.getNumVcpu() - virtualMachine.getNumVcpu()) > modelloVM.getNumVcpu()
                || (diskSpaceMBTOT + virtualMachineDTO.getDiskSpaceMB() - virtualMachine.getDiskSpaceMB()) > modelloVM.getDiskSpaceMB()) {
            throw new InvalidModelException();
        }

        /*non si possono cambiare le risorse di una vm se questa è attiva*/
        if (virtualMachine.isAttiva() && (virtualMachine.getRamMB() != virtualMachineDTO.getRamMB() || virtualMachine.getDiskSpaceMB() != virtualMachineDTO.getDiskSpaceMB() || virtualMachine.getNumVcpu() != virtualMachineDTO.getNumVcpu())) {
            throw new VirtualMachineActiveException();
        }

        /*non si può attivare la vm se questo violerebbe il numero massimo di vm attive contemporaneamente*/
        Long numVmCurrActive = team.getVirtualMachines().stream().filter(vm -> vm.isAttiva()).count();
        if (virtualMachineDTO.isAttiva() && numVmCurrActive + 1 > modelloVM.getMaxActiveVM()) {
            throw new MaxActiveVmException();
        }
        virtualMachine.setRamMB(virtualMachineDTO.getRamMB());
        virtualMachine.setDiskSpaceMB(virtualMachineDTO.getDiskSpaceMB());
        virtualMachine.setNumVcpu(virtualMachineDTO.getNumVcpu());
        virtualMachine.setAttiva(virtualMachineDTO.isAttiva());
        return virtualMachineDTO;

    }

    @Override
    public void deleteVM(Long virtualMachineId, String studentId, String courseName) {

        checkVmId(virtualMachineId);

        VirtualMachine virtualMachine = vmRepository.getOne(virtualMachineId);

        checkCourseName(courseName);
        checkStudentId(studentId);

        Course course = courseRepository.getOne(courseName);
        Student student = studentRepository.getOne(studentId);

        if (!course.getStudents().contains(student)) {
            throw new InvalidStudentException(); //il corso non contiene lo studente
        }

        checkAuthorizationId(studentId);

        if (!virtualMachine.getOwners().contains(student)) {
            throw new InvalidStudentException(); //se lo studente non è owner della vm non la può attivare
        }

        Team team = virtualMachine.getTeam();

        if (!teamRepository.existsById(team.getId())) {
            throw new TeamNotFoundException(); //non deve mai entrare in questa eccezione
        }

        if (!team.getMembers().contains(student)) {
            throw new InvalidStudentException(); //lo studente non fa parte del team
        }
        if (virtualMachine.isAttiva()) {
            throw new VirtualMachineActiveException();
        }
        vmRepository.delete(virtualMachine);

    }

    @Override
    public void shareOwnership(Long virtualMachineId, String studentId, List<String> studentIds) {


        checkVmId(virtualMachineId);

        VirtualMachine virtualMachine = vmRepository.getOne(virtualMachineId);

        checkStudentId(studentId);

        Student student = studentRepository.getOne(studentId);

        String sId = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();

        if (!sId.equals(studentId)) {
            throw new InvalidStudentException(); //l'utente loggato che fa la richiesta è diverso dallo studentId o creatore VM e valore passato nel path non coincidono
        }

        if (!virtualMachine.getOwners().contains(student)) {
            throw new InvalidStudentException(); //lo studente non è l'owner della vm non la può attivare
        }

        Team team = virtualMachine.getTeam();

        if (!teamRepository.existsById(team.getId())) {
            throw new TeamNotFoundException(); //non dovrebbe mai entrare in questa eccezione
        }

        if (!team.getMembers().contains(student)) {
            throw new InvalidStudentException(); //lo studente non fa parte del team
        }

        List<Student> students = studentRepository.findAllById(studentIds);

        for (Student s : students) {
            if (!team.getMembers().contains(s)) {
                throw new InvalidStudentException(); //lo studente non fa parte del team
            } else {
                virtualMachine.addOwner(s);
            }
        }

    }

    @Override
    public List<VirtualMachineDTO> getVirtualMachinesForTeacher(String courseName, Long teamId) {


        String tId = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();


        if (tId == null) {
            throw new InvalidTeacherException();
        }
        if (!teacherRepository.existsById(tId)) {
            throw new TeacherNotFoundException();
        }


        checkCourseName(courseName);
        Teacher teacher = teacherRepository.getOne(tId);
        Course course = courseRepository.getOne(courseName);

        if (!course.getTeachers().contains(teacher)) {
            throw new InvalidTeacherException();
        }

        checkTeamId(teamId);

        Team team = teamRepository.getOne(teamId);

        if (!course.getTeams().contains(team)) {
            throw new TeamNotFoundException();
        }

        return team.getVirtualMachines().stream()
                .map(vm -> modelMapper.map(vm, VirtualMachineDTO.class))
                .collect(Collectors.toList());

    }

    @Override
    public List<VirtualMachineDTO> getVirtualMachinesForStudent(String studentId, Long teamId) {


        checkStudentId(studentId);

        Student student = studentRepository.getOne(studentId);

        checkAuthorizationId(studentId);

        checkTeamId(teamId);

        Team team = teamRepository.getOne(teamId);

        if (!team.getMembers().contains(student)) {
            throw new InvalidStudentException(); //lo studente non fa parte del team
        }

        return team.getVirtualMachines().stream()
                .map(vm -> modelMapper.map(vm, VirtualMachineDTO.class))
                .collect(Collectors.toList());

    }

    @Override
    public List<String> getOwners(Long virtualMachineId, String studentId) {

        checkVmId(virtualMachineId);

        VirtualMachine virtualMachine = vmRepository.getOne(virtualMachineId);

        checkStudentId(studentId);

        Student student = studentRepository.getOne(studentId);

        String sId = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();

        if (!sId.equals(studentId)) {
            throw new InvalidStudentException(); //l'utente loggato che fa la richiesta è diverso dallo studentId o creatore VM e valore passato nel path non coincidono
        }

        if (!virtualMachine.getOwners().contains(student)) {
            throw new InvalidStudentException();
        }

        return virtualMachine.getOwners().stream().map(Student::getId).collect(Collectors.toList());
    }


    private void checkTeacherInCourse(String teacherId, String courseName) {
        checkTeacherId(teacherId);
        Teacher teacher = teacherRepository.getOne(teacherId);
        checkCourseName(courseName);
        Course course = courseRepository.getOne(courseName);
        if (!course.getTeachers().contains(teacher)) {
            throw new InvalidTeacherException();
        }
    }


    @Scheduled(fixedDelay = 20000)
    public void checkExpiredConsegne() {
        List<Consegna> expiredConsegne = consegnaRepository.findAllByScadenzaBefore(new Timestamp(System.currentTimeMillis()));

        Instant instant = Instant.now();
        Timestamp dataConsegna = Timestamp.from(instant);

        List<Student> students = expiredConsegne.stream()
                .flatMap(c -> c.getElaborati().stream())
                .map(Elaborato::getStudent).distinct().collect(Collectors.toList());


        /*
         * per ogni consegna scaduta, per ogni studente, ottengo la lista temporalmente ordinata di elaborati da lui caricati
         * se lo studente non ha consegnato in tempo creo un elaborato con stato CONSEGNATO senza contenuto
         * */
        if (!expiredConsegne.isEmpty()) {
            expiredConsegne.forEach(
                    c -> {
                        students.forEach(s -> {
                            List<Elaborato> elaborati = s.getElaborati().stream().filter(e -> e.getConsegna().equals(c))
                                    .sorted(Comparator.comparing(Elaborato::getDataCaricamento).reversed())
                                    .collect(Collectors.toList());

                            if (!elaborati.isEmpty()) {
                                Elaborato elaborato = elaborati.get(0);

                                if (elaborato.getStato().equals("LETTO") || elaborato.getStato().equals("NULL")) {
                                    Elaborato e = Elaborato.builder()
                                            .consegna(elaborato.getConsegna())
                                            .dataCaricamento(dataConsegna)
                                            .stato("CONSEGNATO")
                                            .student(elaborato.getStudent())
                                            .voto("-")
                                            .possibileRiconsegna("-")
                                            .build();
                                    elaboratoRepository.save(e);

                                }
                            }
                        });
                    }
            );
        }
    }
}
