package com.dd.dda.config;

import com.dd.dda.model.sqldata.Bill;
import com.dd.dda.model.sqldata.User;
import com.dd.dda.repository.NotificationRepository;
import com.dd.dda.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;
import java.util.List;

import static com.dd.dda.DDAApplication.NOT_TEST;

@Slf4j
@Configuration
@Profile(value = {NOT_TEST})
public class ScheduledConfiguration {

    private final UserService userService;
    private final NotificationService notificationService;
    private final MailService mailService;
    private final UserBillVoteService userBillVoteService;
    private final BillService billService;
    private final CommentService commentService;
    private final NotificationRepository notificationRepository;


    public ScheduledConfiguration(UserService userService, NotificationService notificationService, MailService mailService, UserBillVoteService userBillVoteService, BillService billService, CommentService commentService, NotificationRepository notificationRepository) {

        this.userService = userService;
        this.notificationService = notificationService;
        this.mailService = mailService;

        this.userBillVoteService = userBillVoteService;
        this.billService = billService;
        this.commentService = commentService;
        this.notificationRepository = notificationRepository;
    }


    //einmal täglich
    @Scheduled(cron = "0 0 5 1/1 * ?", zone = "CET")
    public void sendVerificationReminder(){
        List<User> users = userService.getUsersCreated3DaysAgo();
        for(User u : users){
            mailService.sendReminderMails(u);
        }
    }


    //alle 45 Minuten
    @Scheduled(cron = "0 */45 * ? * *", zone = "UTC")
    public void rerankAllBills(){
        userBillVoteService.startBillRankingThread();
    }

    //einmal täglich
    @Scheduled(cron = "0 0 4 1/1 * ?", zone = "CET")
    public void dailyBillRankUpdate(){
        billService.updateAvgLikeRatio();
    }


    //einmal täglich
    @Scheduled(cron = "0 0 0 1/1 * ?", zone = "CET")
    public void closeVotes(){
        List<Bill> billsDueToday = billService.getBillsDueToday();
        for(Bill b : billsDueToday){
            List<Integer> votes = userBillVoteService.getVotes(b.getId());
            billService.closeBill(b, votes.get(0), votes.get(1));
        }
    }


    //alle 5 Minuten
    @Scheduled(cron = "0 */5 * ? * *", zone = "UTC")
    public void rerankAllComments(){
        commentService.startCommentRankingThread();
    }

    //einmal täglich
    @Scheduled(cron = "0 0 3 1/1 * ?", zone = "CET")
    public void dailyCommentRankUpdate(){
        commentService.updateAvgLikeRatio();
    }

    //1x täglich
    @Scheduled(cron = "0 0 1 1/1 * ?", zone = "CET")
    public void deleteOldNotofications(){
        Date now = new Date();
        Date d1 = DateUtils.addDays(now, -31);
        Date d2 = DateUtils.addDays(now, -90);
        notificationRepository.deleteOldNotifications(d1, d2);
    }


}
