package com.dd.dda.service;

import com.dd.dda.config.ThreadConfig;
import com.dd.dda.model.exception.DDAException;
import com.dd.dda.model.idclasses.UserBillVoteId;
import com.dd.dda.model.sqldata.Bill;
import com.dd.dda.model.sqldata.UserBillVote;
import com.dd.dda.repository.BillRepository;
import com.dd.dda.repository.UserBillVoteRepository;
import com.dd.dda.thread.BillRankingThread;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserBillVoteService {

    private final UserBillVoteRepository userBillVoteRepository;
    private final BillService billService;
    private final UserService userService;
    private final ParliamentService parliamentService;
    private final BillRepository billRepository;
    private final NotificationService notificationService;


    public UserBillVoteService(UserBillVoteRepository userBillVoteRepository, BillService billService, UserService userService, ParliamentService parliamentService, BillRepository billRepository, NotificationService notificationService) {
        this.userBillVoteRepository = userBillVoteRepository;
        this.billService = billService;
        this.userService = userService;
        this.parliamentService = parliamentService;
        this.billRepository = billRepository;
        this.notificationService = notificationService;
    }

    public void deleteVote(Long user_id, Long bill_id) {
        UserBillVote ubv = getById(user_id, bill_id);
        if(ubv == null){
            throw new DDAException("UserBillVote nicht gefunden");
        }
        userBillVoteRepository.delete(ubv);
    }

    public UserBillVote getById(Long user_id, Long bill_id){

        if (user_id == null || user_id <= 0 || bill_id == null || bill_id <= 0) {
            throw new DDAException("Negative Ids are not allowed");
        }
        UserBillVoteId ubvi = new UserBillVoteId(bill_id, user_id);
        Optional<UserBillVote> ubv = userBillVoteRepository.findById(ubvi);
        if (ubv.isPresent()) {
            return ubv.get();
        }
        return null;
    }


    public UserBillVote vote(Long user_id, Long bill_id, Boolean vote) {
        UserBillVote ubv = getById(user_id, bill_id);
        if (ubv == null) {
            ubv = new UserBillVote(bill_id, user_id);
        }
        ubv.setVote(vote);
        saveUserBilVote(ubv);
        return ubv;

    }

    @Transactional
    public UserBillVote saveUserBilVote(UserBillVote userBillVote){
        return userBillVoteRepository.save(userBillVote);
    }

    public List<Integer> getVotes(Long bill_id) {
        //TODO nur wenn Parlament es erlaubt
        List<UserBillVote> ubvs = userBillVoteRepository.getVotesFor(bill_id);
        int allcnt = ubvs.size();
        int ys = (int) ubvs.stream().filter(ubv -> ubv.getVote() == true  ).count();
        int ns = allcnt - ys;
        List<Integer> result = new ArrayList<>();
        result.add(ys);
        result.add(ns);
        return result;
    }








    public void startBillRankingThread(){
        List<Long> ids = billService.getAncClearBillIdsToRerank();
        if(!ids.isEmpty()) {
            ApplicationContext ctx = new AnnotationConfigApplicationContext(ThreadConfig.class);
            BillRankingThread rankingThread = (BillRankingThread) ctx.getBean("billRankingThread");
            rankingThread.init(ids, billService.getAvgLikeRatio(), billRepository, userBillVoteRepository);
            rankingThread.start();
        }
    }

    public void sendResultNotification(Long bill_id) {
        List<Long> user_ids = userBillVoteRepository.findIdsOfUsersWhoVotedOn(bill_id);
        Bill bill = billService.getBillById(bill_id);
        notificationService.sendVoteResultNotificationBundle(user_ids, bill);
    }
}
