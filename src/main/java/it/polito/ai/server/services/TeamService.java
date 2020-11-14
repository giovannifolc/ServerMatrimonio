package it.polito.ai.server.services;

import it.polito.ai.server.dtos.*;
import it.polito.ai.server.entities.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.io.Reader;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface TeamService {

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    boolean addCourse(CourseDTO course, ModelloVMDTO modelloVMDTO);

    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_STUDENT')")
    Optional<CourseDTO> getCourse(String name);

    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_STUDENT')")
    List<CourseDTO> getAllCourses();

    boolean addStudent(StudentDTO student);

    @PreAuthorize("hasRole('ROLE_STUDENT')")
    Optional<StudentDTO> getStudent(String studentId);

    @PreAuthorize("hasRole('ROLE_STUDENT')")
    List<StudentDTO> getAllStudents();

    List<StudentDTO> getEnrolledStudents(String courseName);

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    List<StudentWithTeamInfoDTO> getEnrolledStudentsWithTeamInfo(String courseName);

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    boolean addStudentToCourse(String studentId, String courseName);

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    void enableCourse(String courseName);

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    void disableCourse(String courseName);


    List<Boolean> enrollAll(List<String> studentIds, String courseName);

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    List<Boolean> enrollAll(Reader r, String courseName);

    @PreAuthorize("hasRole('ROLE_STUDENT')")
    List<CourseDTO> getCourses(String studentId);

    @PreAuthorize("hasRole('ROLE_STUDENT')")
    List<TeamDTO> getTeamsForStudent(String studentId);

    @PreAuthorize("hasRole('ROLE_STUDENT')")
    List<StudentDTO> getMembers(Long teamId);

    @PreAuthorize("hasRole('ROLE_STUDENT')")
    TeamDTO proposeTeam(String courseId, String name, List<String> memberIds, Timestamp expiryDate);

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    List<TeamDTO> getTeamsForCourse(String courseName);

    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_STUDENT')")
    List<StudentDTO> getStudentsInTeams(String courseName);

    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_STUDENT')")
    List<StudentDTO> getAvailableStudents(String courseName);

    void activateTeam(Long teamId);

    void evictTeam(Long teamId);

    void evictTeams(List<Long> teamIds);

    boolean addTeacher(TeacherDTO teacher);

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    Optional<TeacherDTO> getTeacher(String teacherId);

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    List<TeacherDTO> getAllTeachers();

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    void addTeacherForCourse(String teacherId, String courseName);

    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_STUDENT')")
    List<TeacherDTO> getTeachersForCourse(String courseName);

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    List<CourseDTO> getCoursesForTeacher(String teacherId);

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    void deleteCourse(String courseName);

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    void updateCourse(CourseDTO courseDTO);

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    boolean removeStudentFromCourse(String studentId, String courseName);

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    boolean updateModelloVM(String courseName, ModelloVMDTO modelloVMDTO);

    @PreAuthorize("hasRole('ROLE_STUDENT')")
    VirtualMachineDTO createVM(VirtualMachineDTO virtualMachineDTO, String courseName, String studentId, Long teamId);

    @PreAuthorize("hasRole('ROLE_STUDENT')")
    List<PropostaTeamDTO> getTeamRequests(String courseName, String sId);

    @PreAuthorize("hasRole('ROLE_STUDENT')")
    void setImageForStudent(String studentId, Byte[] image);

    @PreAuthorize("hasRole('ROLE_STUDENT')")
    Byte[] getImageFromStudent(String studentId);

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    void setImageForTeacher(String teacherId, Byte[] image);

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    Byte[] getImageFromTeacher(String teacherId);

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    void createConsegna(String scadenza, String courseName, String nomeConsegna, Byte[] contenuto) throws ParseException;

    @PreAuthorize("hasRole('ROLE_STUDENT')")
    boolean consegnaElaborato(Long consegnaId, String studentId, Byte[] contenuto);

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    void correggereElaborato(Long elaboratoId, String courseName,
                             Long consegnaId, Byte[] contenuto, String possibileRiconsegna, String voto);

    @PreAuthorize("hasRole('ROLE_STUDENT')")
    List<ConsegnaDTO> getConsegne(String studentId, String courseName);

    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_STUDENT')")
    List<ConsegnaDTO> getConsegneForCourse(String courseName);

    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_STUDENT')")
    Byte[] getConsegnaForCourse(String consegnaId);

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    List<ElaboratoForTeacherDTO> getElaborati(String courseName, Long consegnaId);

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    List<ElaboratoDTO> getUltimiElaborati(String courseName, Long consegnaId);

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    Byte[] getElaborato(Long elaboratoId, String courseName, String studentId, Long consegnaId);

    @PreAuthorize("hasRole('ROLE_STUDENT')")
    VirtualMachineDTO manageVM(VirtualMachineDTO virtualMachineDTO, Long virtualMachineId, String studentId, String courseName);

    @PreAuthorize("hasRole('ROLE_STUDENT')")
    void deleteVM(Long virtualMachineId, String studentId, String courseName);

    @PreAuthorize("hasRole('ROLE_STUDENT')")
    void shareOwnership(Long virtualMachineId, String studentId, List<String> students); //condivde la ownership solo con una lista di studenti.

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    List<VirtualMachineDTO> getVirtualMachinesForTeacher(String courseName, Long teamId);

    @PreAuthorize("hasRole('ROLE_STUDENT')")
    List<VirtualMachineDTO> getVirtualMachinesForStudent(String studentId, Long teamId);

    @PreAuthorize("hasRole('ROLE_STUDENT')")
    List<String> getOwners(Long virtualMachineId, String studentId);

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    List<ElaboratoDTO> getElaboratiConsegnatiFromStudentId(String courseName, Long consegnaId, String studentId);

    @PreAuthorize("hasRole('ROLE_STUDENT')")
    Byte[] leggiCorrezione(Long consegnaId, String studentId, String elabId);

    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_STUDENT')")
    ModelloVMDTO getModelloVM(String courseName);

    @PreAuthorize("hasRole('ROLE_TEACHER')")
    List<StudentDTO> getNotEnrolledStudents(String name);

    @PreAuthorize("hasRole('ROLE_STUDENT')")
    TeamDTO getTeamForStudentAndCourse(String studentId, String courseName);

    @PreAuthorize("hasRole('ROLE_STUDENT')")
    List<Long> getOwnedVM(String studentId, String courseName, Long teamId);

    @PreAuthorize("hasRole('ROLE_STUDENT')")
    List<ElaboratoDTO> getElaboratiForStudent(String studentId, Long consegnaId);

    @PreAuthorize("hasRole('ROLE_STUDENT')")
    Byte[] getConsegna(long consegnaId, String id);

    @PreAuthorize("hasRole('ROLE_STUDENT')")
    Byte[] getElaboratoById(String elaboratoId, String studentId);

    @PreAuthorize("hasRole('ROLE_STUDENT')")
    List<StudentDTO> getAvailableStudentsForVM(String studentId, String courseName, Long teamId, Long vmId);

    @PreAuthorize("hasRole('ROLE_STUDENT')")
    Boolean checkAcceptedRequest(String studentId, String courseName);

    @PreAuthorize("hasAnyRole('ROLE_STUDENT','ROLE_TEACHER')")
    byte[] getVM() throws IOException;

}
