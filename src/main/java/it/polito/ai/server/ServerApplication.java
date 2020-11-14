package it.polito.ai.server;

import it.polito.ai.server.entities.Student;
import it.polito.ai.server.entities.Teacher;
import it.polito.ai.server.entities.User;
import it.polito.ai.server.repositories.*;
import it.polito.ai.server.services.TeamService;
import it.polito.ai.server.services.UserDetailsServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@EnableScheduling
@SpringBootApplication
public class ServerApplication {
    @Autowired
    TeamService teamService;

    @Autowired
    TeacherRepository teacherRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    private PasswordEncoder bcryptEncoder;

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Bean
    CommandLineRunner runner(TeamService teamService, UserDetailsServiceImpl userDetailsServiceImpl) {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
                {
                    if(userRepository.findAll().size() == 0) {
                        List<User> users = new ArrayList<>();
                        List<Student> students = new ArrayList<>();

                        users.add(User.builder()
                                .password(bcryptEncoder.encode("123456"))
                                .role("ROLE_TEACHER")
                                .username("123456")
                                .email("d123456@polito.it").build());
                        users.add(User.builder()
                                .password(bcryptEncoder.encode("123456"))
                                .role("ROLE_STUDENT")
                                .username("1")
                                .email("s1@studenti.polito.it").build());
                        users.add(User.builder()
                                .password(bcryptEncoder.encode("123456"))
                                .role("ROLE_STUDENT")
                                .username("2")
                                .email("s2@studenti.polito.it").build());
                        users.add(User.builder()
                                .password(bcryptEncoder.encode("123456"))
                                .role("ROLE_STUDENT")
                                .username("3")
                                .email("s3@studenti.polito.it").build());
                        users.add(User.builder()
                                .password(bcryptEncoder.encode("123456"))
                                .role("ROLE_STUDENT")
                                .username("4")
                                .email("s4@studenti.polito.it").build());
                        users.add(User.builder()
                                .password(bcryptEncoder.encode("123456"))
                                .role("ROLE_STUDENT")
                                .username("5")
                                .email("s5@studenti.polito.it").build());
                        users.add(User.builder()
                                .password(bcryptEncoder.encode("123456"))
                                .role("ROLE_STUDENT")
                                .username("6")
                                .email("s6@studenti.polito.it").build());

                        students.add(Student.builder()
                                .id("1")
                                .name("Marco")
                                .firstName("Polo").build());
                        students.add(Student.builder()
                                .id("2")
                                .name("Giovanni")
                                .firstName("Bianchi").build());
                        students.add(Student.builder()
                                .id("3")
                                .name("Fabrizio")
                                .firstName("Verdi").build());
                        students.add(Student.builder()
                                .id("4")
                                .name("Bruno")
                                .firstName("Bruni").build());
                        students.add(Student.builder()
                                .id("5")
                                .name("Mario")
                                .firstName("Zara").build());
                        students.add(Student.builder()
                                .id("6")
                                .name("Giorgio")
                                .firstName("Giorgi").build());

                        Teacher teacher = Teacher.builder()
                                .id("123456")
                                .firstName("Rossi")
                                .name("Mario").build();


                        teacherRepository.save(teacher);

                        for (User u : users) {
                            userRepository.save(u);
                        }
                        for (Student s : students) {
                            studentRepository.save(s);
                        }
                    }
                }
                ;
            }
        };
    }

    @Bean
    ModelMapper modelMapper() {
        return new ModelMapper();
    }

}
