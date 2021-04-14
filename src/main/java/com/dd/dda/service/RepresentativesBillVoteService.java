package com.dd.dda.service;

import com.dd.dda.model.exception.DDAException;
import com.dd.dda.model.sqldata.Bill;
import com.dd.dda.model.sqldata.RepresentativesBillVote;
import com.dd.dda.repository.RepresentativesBillVoteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class RepresentativesBillVoteService {

    private final RepresentativesBillVoteRepository representativesBillVoteRepository;
    private final BillService billService;
    private final PartyParliamentSeatsService partyParliamentSeatsService;

    public RepresentativesBillVoteService(RepresentativesBillVoteRepository representativesBillVoteRepository, BillService billService, PartyParliamentSeatsService partyParliamentSeatsService) {
        this.representativesBillVoteRepository = representativesBillVoteRepository;
        this.billService = billService;
        this.partyParliamentSeatsService = partyParliamentSeatsService;
    }

    public RepresentativesBillVote createVote(Long id, Long bill_id, Long party_id, int yesvotes, int novotes, int abstvotes) {

        RepresentativesBillVote rbv = new RepresentativesBillVote();
        rbv.setBill_id(bill_id);
        rbv.setParty_id(party_id);
        rbv.setYesvotes(yesvotes);
        rbv.setNovotes(novotes);
        rbv.setAbstinences(abstvotes);

        Bill b = billService.getBillById(bill_id);
        int seats = partyParliamentSeatsService.getSeatsAtTime(b.getParliament().getId(), b.getDate_vote()).stream().filter(s -> s.getParty_id() == party_id).findAny().get().getSeats();

        if(yesvotes + novotes + abstvotes != seats){
            throw new DDAException("Stimmenanzahl stimmt nicht mit Anzahl der Sitzen überein");
        }

        representativesBillVoteRepository.save(rbv);
        return rbv;

    }

    public List<RepresentativesBillVote> getVotes(Long bill_id) {
        return representativesBillVoteRepository.getVotesFor(bill_id);

    }
}
