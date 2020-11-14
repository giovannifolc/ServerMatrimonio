package it.polito.ai.server.controllers;

import it.polito.ai.server.dtos.CourseDTO;
import it.polito.ai.server.dtos.StudentDTO;
import it.polito.ai.server.dtos.TeamDTO;
import it.polito.ai.server.services.exceptions.InvalidTeamException;
import it.polito.ai.server.services.exceptions.TeamNotFoundException;
import it.polito.ai.server.services.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/API/teams")
public class TeamController {

    @Autowired
    TeamService teamService;

    /**
     * Ottiene tutti i membri di un team dato il teamId
     * @param id identificativo del team
     * @return lista di studenti
     * Accessibile ai ruoli: Student
     */
    @GetMapping("/{id}/members")
    public List<StudentDTO> getMembers(@PathVariable Long id){
        try{
            return teamService.getMembers(id);
        }catch (TeamNotFoundException | InvalidTeamException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, id.toString());
        }
    }
}
