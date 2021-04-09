package com.dd.dda.service;

import com.dd.dda.model.sqldata.Party;
import com.dd.dda.repository.PartyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class PartyService {

    private final PartyRepository partyRepository;

    public PartyService(PartyRepository partyRepository) {
        this.partyRepository = partyRepository;
    }

    public Party getOrCreateParty(String name){
        List<Party> parties = partyRepository.findAll();
        for(Party p : parties){
            if(p.getName().toLowerCase().equals(name.toLowerCase())){
                return p;
            }
        }
        Party p = new Party();
        p.setName(name);
        partyRepository.save(p);
        return p;
    }

    public Party getPartyByName(String name){
        List<Party> parties = partyRepository.findAll();
        for(Party p : parties){
            if(p.getName().toLowerCase().equals(name.toLowerCase())){
                return p;
            }
        }
        return null;

    }

    public Party createNewParty(String inputParty) {
        Party p = new Party();
        p.setName(inputParty);
        partyRepository.save(p);
        return p;
    }

    public Party getById(long party_id) {
        return partyRepository.findById(party_id).get();
    }

    public List<Party> getAll() {
        return partyRepository.findAll();
    }
}

