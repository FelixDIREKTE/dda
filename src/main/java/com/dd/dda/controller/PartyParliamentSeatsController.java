package com.dd.dda.controller;

import com.dd.dda.model.exception.DDAException;
import com.dd.dda.model.sqldata.Party;
import com.dd.dda.model.sqldata.PartyParliamentSeats;
import com.dd.dda.service.PartyParliamentSeatsService;
import com.dd.dda.service.PartyService;
import com.dd.dda.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/seats")
public class PartyParliamentSeatsController {

    private final PartyParliamentSeatsService seatsService;
    private final PartyService partyService;
    private final UserService userService;

    public PartyParliamentSeatsController(PartyParliamentSeatsService seatsService, PartyService partyService, UserService userService) {
        this.seatsService = seatsService;
        this.partyService = partyService;
        this.userService = userService;
    }


    @PutMapping("/{id}/create")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<PartyParliamentSeats> create(@PathVariable(value = "id") Long id,
                                           @RequestParam(value = "from_date") String from_date,
                                           @RequestParam(value = "seats") Integer seats,
                                           @RequestParam(value = "party") String party,
                                           @RequestParam(value = "parliament_id") Long parliament_id
    ) {
        log.info("enter create seats " + id);
        if(!userService.getUserByIdUnencrypted(id).isAdmin()){
            throw new DDAException("Keine Berechtigung!");
        }


        log.info("Enter into create seats");
        if (from_date == null || from_date.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }
        if (party == null || party.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }
        if (seats == null) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }
        if (parliament_id == null || parliament_id <= 0) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }
        Party pa = partyService.getOrCreateParty(party);
        PartyParliamentSeats result = seatsService.createSeats(id, from_date, seats, pa, parliament_id);
        return ResponseEntity.ok(result);
    }



    @GetMapping("{id}/getSeats")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<List<PartyParliamentSeats>> getSeats(@PathVariable(value = "id") Long id,
                                                     @RequestParam(value = "parliament_id") long parliament_id

    ) {
        log.info("enter PartyParliamentSeats " + id);
        return ResponseEntity.ok(seatsService.getSeats(parliament_id));

    }

    @GetMapping("{id}/getSeatsAtTime")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<List<PartyParliamentSeats>> getSeatsAtTime(@PathVariable(value = "id") Long id,
                                                                     @RequestParam(value = "parliament_id") long parliament_id,
                                                                     @RequestParam(value = "date") String date
                                                                     ) {
        log.info("enter PartyParliamentSeats " + id);
        return ResponseEntity.ok(seatsService.getSeatsAtTime(parliament_id, date));

    }


    @GetMapping("getParty")
    //@PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<Party> getParty(@RequestParam(value = "party_id") long party_id

    ) {
        return ResponseEntity.ok(partyService.getById(party_id));
    }

    @GetMapping("{id}/getParties")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<List<Party>> getParties(@PathVariable(value = "id") Long id
    ) {
        log.info("enter PartyParliamentSeats " + id);
        return ResponseEntity.ok(partyService.getAll());
    }


    @GetMapping("{id}/getPartiesInParliament")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<List<Party>> getPartiesInParliament(@PathVariable(value = "id") Long id,
                                                              @RequestParam(value = "parliament_id") Long parliament_id
    ) {
        log.info("enter PartyParliamentSeats " + id);
        return ResponseEntity.ok(seatsService.getPartiesInParliament(parliament_id));
    }




    @DeleteMapping("{id}/delete")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<Boolean> delete(@PathVariable(value = "id") Long id,
                                          @RequestParam(value = "from_date") String from_date,
                                          @RequestParam(value = "party_id") Long party_id,
                                          @RequestParam(value = "parliament_id") Long parliament_id
    )  {
        if(!userService.getUserByIdUnencrypted(id).isAdmin()){
            throw new DDAException("Keine Berechtigung!");
        }

        log.info("enter delete " + id);
        seatsService.deleteSeat(id, from_date, party_id, parliament_id);
        return ResponseEntity.ok(true);
    }




}
