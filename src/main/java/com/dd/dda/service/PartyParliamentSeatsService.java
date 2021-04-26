package com.dd.dda.service;

import com.dd.dda.model.exception.DDAException;
import com.dd.dda.model.idclasses.PartyParliamentSeatsId;
import com.dd.dda.model.sqldata.Party;
import com.dd.dda.model.sqldata.PartyParliamentSeats;
import com.dd.dda.repository.PartyParliamentSeatsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PartyParliamentSeatsService {

    private final PartyParliamentSeatsRepository partyParliamentSeatsRepository;
    private final UtilService utilService;
    private final PartyService partyService;


    public PartyParliamentSeatsService(PartyParliamentSeatsRepository partyParliamentSeatsRepository, UtilService utilService, PartyService partyService) {
        this.partyParliamentSeatsRepository = partyParliamentSeatsRepository;
        this.utilService = utilService;
        this.partyService = partyService;
    }

    public PartyParliamentSeats createSeats(Long id, String from_date, Integer seats, Party pa, Long parliament_id) {
        PartyParliamentSeats ppseats = new PartyParliamentSeats();
        ppseats.setSeats(seats);
        ppseats.setParty_id(pa.getId());
        Date d = utilService.parseDate(from_date);
        ppseats.setFrom_date(d);
        ppseats.setParliament_id(parliament_id);
        partyParliamentSeatsRepository.save(ppseats);
        return ppseats;
    }

    public List<PartyParliamentSeats> getSeats(long parliament_id) {


        List<PartyParliamentSeats> result = partyParliamentSeatsRepository.getSeatsFor(parliament_id);
        result.sort(Comparator.comparing(PartyParliamentSeats::getFrom_date).reversed());
        return result;

    }




    public void deleteSeat(Long id, String from_date, Long pa, Long parliament_id) {
        Date d = utilService.parseDate(from_date);

        PartyParliamentSeatsId ppseatsId = new PartyParliamentSeatsId(pa, parliament_id, d);


        PartyParliamentSeats ubv = getById(d, pa, parliament_id);
        if(ubv == null){
            throw new DDAException("PartyParliamentSeats nicht gefunden");
        }
        partyParliamentSeatsRepository.delete(ubv);
    }

    public PartyParliamentSeats getById(Date d, Long pa, Long parliament_id){

        PartyParliamentSeatsId ppseatsId = new PartyParliamentSeatsId(pa, parliament_id, d);

        Optional<PartyParliamentSeats> ubv = partyParliamentSeatsRepository.findById(ppseatsId);
        if (ubv.isPresent()) {
            return ubv.get();
        }
        return null;
    }

    public List<PartyParliamentSeats> getSeatsAtTime(long parliament_id, String dateS) {
        Date date = utilService.parseJsDate(dateS);
        return getSeatsAtTime(parliament_id, date);
    }

    public List<PartyParliamentSeats> getSeatsAtTime(long parliament_id, Date date) {
        if(date == null){
            return new ArrayList<>();
        }
        Map<Date, List<PartyParliamentSeats>> xx = partyParliamentSeatsRepository.getSeatsFor(parliament_id).stream().collect(Collectors.groupingBy(PartyParliamentSeats::getFrom_date));
        Optional<Date> maxdate = xx.keySet().stream().filter(d -> d.compareTo(date) < 0).max(Date::compareTo);
        if(maxdate.isEmpty()) {
            return new ArrayList<>();
        }
        List<PartyParliamentSeats> result = xx.get(maxdate.get()).stream().filter(pps -> pps.getSeats() > 0).collect(Collectors.toList());
        return result;
    }




    public List<Party> getPartiesInParliament(Long parliament_id) {
        List<PartyParliamentSeats> seats = getSeats(parliament_id);
        List<Party> result = seats.stream().map(s -> partyService.getById(s.getParty_id())).distinct().collect(Collectors.toList());
        return result;
    }
}
