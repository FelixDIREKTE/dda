package com.dd.dda.service;

import com.dd.dda.model.exception.DDAException;
import com.dd.dda.model.sqldata.Bill;
import com.dd.dda.model.sqldata.Parliament;
import com.dd.dda.model.sqldata.Party;
import com.dd.dda.model.sqldata.User;
import com.dd.dda.repository.BillRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BillService {

    private final BillRepository billRepository;
    private final UserService userService;
    private final ParliamentService parliamentService;
    private final SimpleDateFormat dateCsvFormat;
    private final PartyService partyService;
    private final UtilService utilService;
    private final Set<Long> billIdsToRerank;
    private final HtmlConverterService htmlConverterService;

    public Double getAvgLikeRatio() {
        if(avgLikeRatio == null){
            updateAvgLikeRatio();
        }
        return avgLikeRatio;
    }

    public void updateAvgLikeRatio() {
        Double res = billRepository.getAvgLikeRatio();
        if(res == null){
            res = 0.;
        }
        setAvgLikeRatio( res );
    }



    public void setAvgLikeRatio(Double avgLikeRatio) {
        this.avgLikeRatio = avgLikeRatio;
    }

    private Double avgLikeRatio;

    public BillService(BillRepository billRepository, UserService userService, ParliamentService parliamentService, PartyService partyService, UtilService utilService, HtmlConverterService htmlConverterService) {
        this.billRepository = billRepository;
        this.userService = userService;
        this.parliamentService = parliamentService;
        this.partyService = partyService;
        this.utilService = utilService;
        this.htmlConverterService = htmlConverterService;
        this.dateCsvFormat = new SimpleDateFormat("dd.MM.yyyy");
        dateCsvFormat.setTimeZone(TimeZone.getDefault());
        this.billIdsToRerank = new ConcurrentHashSet<Long>();
    }

    public List<Long> getAncClearBillIdsToRerank(){
        List<Long> result = new ArrayList<>(billIdsToRerank);
        billIdsToRerank.clear();
        return result;
    }

    public Bill createNewBill(String title, String abstra, Long userid, long parliament_id, int parliament_role, String inputParty, String inputType, String inputVorgang, String inputDatumVorgelegt, String inputDatumAbstimm, int categoryBits) {
        if(parliament_role == 0 && (inputDatumAbstimm == null || inputDatumAbstimm.isEmpty())){
            throw new DDAException("Datum der Abstimmung muss gegeben sein");
        }

        Bill bill = new Bill();
        bill.setCreated_time(new Date());
        bill.setName(title);
        bill.setAbstr(htmlConverterService.stringToHtml(abstra));
        bill.setCategories_bitstring(categoryBits);
        User creator = userService.getUserByIdUnencrypted(userid);
        if(creator == null){
            return null;
        }
        bill.setCreated_by(creator);
        Parliament p = parliamentService.getParliamentById(parliament_id);
        if(p == null){
            return null;
        }
        bill.setParliament(p);
        bill.setParliament_role(parliament_role);

        if(inputType != null && !inputType.isEmpty()){
            bill.setBilltype(inputType);
        }

        if(inputVorgang != null && !inputVorgang.isEmpty()){
            bill.setProcedurekey(inputVorgang);
        }

        if(inputDatumVorgelegt != null && !inputDatumVorgelegt.isEmpty()){
            Date bd = null;
            try {
                bd = dateCsvFormat.parse(inputDatumVorgelegt);
            } catch (ParseException e) {
                throw new DDAException("Falsches Datumsformat");
            }
            bill.setDate_presented(bd);
        }

        if(inputDatumAbstimm != null && !inputDatumAbstimm.isEmpty()){
            Date bd = null;
            try {
                bd = dateCsvFormat.parse(inputDatumAbstimm);
            } catch (ParseException e) {
                throw new DDAException("Falsches Datumsformat");
            }
            bill.setDate_vote(bd);
        }


        if(inputParty != null && !inputParty.isEmpty()){

            Party pa = partyService.getPartyByName(inputParty);
            if(pa == null){
                throw new DDAException("Unbekannte Partei " + inputParty);
            }

            bill.setParty(pa);
        }

        bill.setReadCount(0);
        bill.setRead_detail_count(0);
        bill.setRelative_value(0);
        bill.setRanking(getAvgLikeRatio());

        billRepository.save(bill);
        return bill;

    }


    public Bill getBillById(Long id) {
        if (id == null || id <= 0) {
            throw new DDAException("Negative Ids are not allowed");
        }

        Optional<Bill> bill = billRepository.findById(id);
        if (bill.isPresent()) {
            Bill userFound = bill.get();
            return userFound;
        }
        return null;
    }

    public List<Bill> getRankedBills(long user_id, long parliament_id, long parliament_role) {
        User u = userService.getUserByIdUnencrypted(user_id);
        if(parliament_role == 0){
            //Ranke Gesetze
            Date today = new Date();
            List<Bill> result = new ArrayList<>();
            List<Bill> futureBills = billRepository.getFutureBillsForParliamentOrderedByDateVote(parliament_id, parliament_role, today);
            List<Bill> pastBills = billRepository.getPastBillsForParliamentOrderedByDateVote(parliament_id, parliament_role, today);
            futureBills.stream().forEach(b -> b.customRankFutureBills(u, today));
            pastBills.stream().forEach(b -> b.customRankPastBills(u, today));
            result.addAll(futureBills);
            result.addAll(pastBills);
            result = result.stream().sorted(Comparator.comparingDouble(Bill::getCustomRanking)).collect(Collectors.toList());
            Collections.reverse(result);
            return result;

        } else {
            if(parliament_role == 1){
                //Ranke Initiativen
                List<Bill> result = billRepository.getBillsForParliamentOrderedByRanking(parliament_id, parliament_role);
                result.stream().forEach(b -> b.customRankInitiatives(u));
                result = result.stream().sorted(Comparator.comparingDouble(Bill::getCustomRanking)).collect(Collectors.toList());
                Collections.reverse(result);
                return result;

            } else {
                if(parliament_role == 2){
                    //Ranke Diskussionen
                    List<Bill> result = billRepository.getBillsForParliamentOrderedByRanking(parliament_id, parliament_role);
                    result.stream().forEach(b -> b.customRankDiscussion(u));
                    result = result.stream().sorted(Comparator.comparingDouble(Bill::getCustomRanking)).collect(Collectors.toList());
                    Collections.reverse(result);
                    return result;
                } else {
                    throw new DDAException("Illegal parliament_role" + parliament_role);
                }
            }

        }
    }

    public String updateBill(String title, String abstr, Long bill_id, Long user_id, String inputParty, String inputType, String inputVorgang, String inputDatumVorgelegt, String inputDatumAbstimm, int categoryBits) {

        Bill bill = getBillById(bill_id);
        bill.setCategories_bitstring(categoryBits);

        if(! (      user_id == bill.getCreated_by().getId()
                || (bill.getParliament_role() == 0 && userService.getUserByIdUnencrypted(user_id).isAdmin())
              )  ){
            throw  new DDAException("Keine Berechtigung!");
        }
        if(title != null && !title.isEmpty()) {
            bill.setName(title);
        }
        if(abstr != null && !abstr.isEmpty()) {
            bill.setAbstr(htmlConverterService.stringToHtml(abstr));
        }

        if(inputType != null && !inputType.isEmpty()){
            bill.setBilltype(inputType);
        }

        if(inputVorgang != null && !inputVorgang.isEmpty()){
            bill.setProcedurekey(inputVorgang);
        }

        if(inputDatumVorgelegt != null && !inputDatumVorgelegt.isEmpty()){
            Date bd = null;
            try {
                bd = dateCsvFormat.parse(inputDatumVorgelegt);
            } catch (ParseException e) {
                return "Falsches Datumsformat";
            }
            bill.setDate_presented(bd);
        }

        if(inputDatumAbstimm != null && !inputDatumAbstimm.isEmpty()){
            Date bd = null;
            try {
                bd = dateCsvFormat.parse(inputDatumAbstimm);
            } catch (ParseException e) {
                return "Falsches Datumsformat";
            }
            bill.setDate_vote(bd);
        }

        if(inputParty != null && !inputParty.isEmpty()){

            Party pa = partyService.getPartyByName(inputParty);
            if(pa == null){
                throw new DDAException("Unbekannte Partei " + inputParty);
            }
            bill.setParty(pa);
        }

        billRepository.save(bill);
        return "ok";
    }

    public Bill getBill(Long bill_id) {
        Optional<Bill> b = billRepository.findById(bill_id);
        if(b.isEmpty()){
            throw new DDAException("No Bill with id "+ bill_id);
        }
        return b.get();
    }

    public void deleteBill(Long user_id, Long bill_id) {
        Bill bill = getBillById(bill_id);

        if(user_id != bill.getCreated_by().getId()){
            throw  new DDAException("Keine Berechtigung!");
        }
        billRepository.delete(bill);

    }

    public void closeBill(Bill b, Integer yesvotes, Integer novotes) {
        b.setFinal_no_votes(novotes);
        b.setFinal_yes_votes(yesvotes);
        billRepository.save(b);
    }

    public List<Bill> getBillsDueToday() {
        Date today = utilService.trimDate(DateUtils.addHours(new Date(), 7));
        log.info("getBillsDue " + today);
        return billRepository.getBillsDueOn(today);
    }







//getAvgLikeRatio


    public List<Long> getBillIdsOfRatingsForParliament(Long id, Long parliament_id, int parliament_role ) {
        List<Bill> ratedBills = billRepository.getBillsInParliamentVotedByUser(id, parliament_id, parliament_role);
        List<Bill> ownBills = billRepository.getBillsBy(id, parliament_id, parliament_role);

        List<Bill> result = new ArrayList<>();

        result.addAll(ownBills);
        result.addAll(ratedBills);
        return result.stream().map(c -> c.getId()).distinct().collect(Collectors.toList());
    }

    public void addReads(Long[] readBillsIds, Long readBillDetailId) {
        billRepository.addRead(readBillsIds);
        billRepository.addDetailRead(readBillDetailId);
        billIdsToRerank.addAll(Arrays.asList(readBillsIds));
    }

    public List<Bill> getBillSearch(Long user_id, Long parliament_id, Long parliament_role, String searchterm) {
        List<Bill> result = billRepository.getBillsForParliamentOrderedBySearchTerm(searchterm, parliament_id, parliament_role);
        return result;
    }



}
