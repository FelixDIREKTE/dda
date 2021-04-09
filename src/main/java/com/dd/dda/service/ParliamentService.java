package com.dd.dda.service;

import com.dd.dda.model.exception.DDAException;
import com.dd.dda.model.sqldata.Parliament;
import com.dd.dda.model.sqldata.UserParliamentAccess;
import com.dd.dda.repository.LocalFranchiseRepository;
import com.dd.dda.repository.ParliamentRepository;
import com.dd.dda.repository.UserParliamentAccessRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ParliamentService {

    private final ParliamentRepository parliamentRepository;
    private final UserParliamentAccessRepository userParliamentAccessRepository;
    private final LocalFranchiseRepository localFranchiseRepository;

    public ParliamentService(ParliamentRepository parliamentRepository, UserParliamentAccessRepository userParliamentAccessRepository, LocalFranchiseRepository localFranchiseRepository) {
        this.parliamentRepository = parliamentRepository;
        this.userParliamentAccessRepository = userParliamentAccessRepository;
        this.localFranchiseRepository = localFranchiseRepository;
    }

    public List<Parliament> getAll(){
        return parliamentRepository.findAll();
    }

    public Parliament getParliamentById(Long id) {
        if (id == null || id <= 0) {
            throw new DDAException("Negative Ids are not allowed");
        }

        Optional<Parliament> parliament = parliamentRepository.findById(id);
        if (parliament.isPresent()) {
            Parliament userFound = parliament.get();
            return userFound;
        }
        return null;
    }

    public List<Parliament> getSubParliaments(Long parliament_id) {
        return parliamentRepository.findParliamentByUpperParliamentId(parliament_id);
    }

    public void deleteVoteAccesses(Long user_id) {
        List<UserParliamentAccess> upas = userParliamentAccessRepository.findByUserId(user_id);
        userParliamentAccessRepository.deleteAll(upas);

    }

    public void addVoteAccesses(Long user_id, List<Long> parliamentIds) {
        giveDefaultAccess(user_id);
        List<UserParliamentAccess> upas = new ArrayList<>();
        //EU-Parlament und Bundestag
        for (Long pid : parliamentIds) {
            if (pid <= 0){
                continue;
            }
            UserParliamentAccess upa = new UserParliamentAccess(pid, user_id);
            upa.setVoteaccess(true);
            upas.add(upa);
        }
        userParliamentAccessRepository.saveAll(upas);
    }

    public void giveDefaultAccess(Long user_id) {
        UserParliamentAccess upa1 = new UserParliamentAccess(2L, user_id);
        upa1.setVoteaccess(true);
        UserParliamentAccess upa0 = new UserParliamentAccess(1L, user_id);
        upa0.setVoteaccess(true);
        userParliamentAccessRepository.save(upa0);
        userParliamentAccessRepository.save(upa1);

    }

    public List<Parliament> getEligibleParliaments(Long user_id) {
        return Arrays.asList( parliamentRepository.findById(2L).get());
        // TODO das hier reinnehmen wenn Leute zum Einspeisen da sine
        /*
        List<UserParliamentAccess> upas = userParliamentAccessRepository.findByUserId(user_id);
        List<Long> pids = upas.stream().map(upa -> upa.getParliament_id()).collect(Collectors.toList());
        List<Parliament> result = parliamentRepository.findAllById(pids).stream().filter(pa -> !pa.getName().contains("kreisfrei") &&
                (!(pa.getUpper_parliament() != null && pa.getUpper_parliament().getName().equals(pa.getName())))
                 ).collect(Collectors.toList());
        return result;*/
    }


    public List<Parliament> getEligibleParliamentsComplete(Long user_id) {
        List<UserParliamentAccess> upas = userParliamentAccessRepository.findByUserId(user_id);
        List<Long> pids = upas.stream().map(upa -> upa.getParliament_id()).collect(Collectors.toList());
        List<Parliament> result = parliamentRepository.findAllById(pids).stream().filter(pa -> !pa.getName().contains("kreisfrei")
        ).collect(Collectors.toList());
        return result;
    }




    public boolean hasAllParlimamentAccess(Long user_id) {
        return userParliamentAccessRepository.findByUserId(user_id).size() >= 6;
    }

    public boolean parliamentsConsistentWithZipcode(List<Long> parliamentIds, String plz) {
        List<Long> plzIds = localFranchiseRepository.findAllByZip(plz).stream().map(lf -> lf.getParliament_id()).collect(Collectors.toList());
        //entweder plz unbekannt
        if(plzIds.isEmpty()){
            return true;
        }

        //oder 1 Übereinstimmung
        for(Long a : plzIds){
            for(Long b : parliamentIds){
                if(a.equals(b)){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean sameParliaments(Long user_id, List<Long> parliamentIds) {
        return getEligibleParliamentsComplete(user_id).stream().map(p -> p.getId()).collect(Collectors.toList()).containsAll(parliamentIds);
    }
}
