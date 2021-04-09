package com.dd.dda.service;

import com.dd.dda.model.sqldata.Bill;
import com.dd.dda.model.sqldata.Comment;
import com.dd.dda.model.sqldata.Notification;
import com.dd.dda.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final HtmlConverterService htmlConverterService;

    public NotificationService(NotificationRepository notificationRepository, HtmlConverterService htmlConverterService) {
        this.notificationRepository = notificationRepository;
        this.htmlConverterService = htmlConverterService;
    }

    public List<Notification> getNotifications(Long id) {
        List<Notification> result = notificationRepository.getNotifications(id);
        return result;
    }

    public void sendNotification(Long user_id, String msg){
        Notification notification = new Notification();
        notification.setReceiver_user_id(user_id);
        notification.setMessage(msg);
        notification.setCreated_time(new Date());
        notificationRepository.save(notification);
    }

    //sende wenn verifizierter Nutzer antowrtet,
    // !!und wenn Nutzer der schon geantwortet hat verifiziert wird
    public void sendReplyNotification(Comment comment){
        String cmttxt = htmlConverterService.htmlToString(comment.getText());
        cmttxt = comment.getText().length() < 128 ? cmttxt : cmttxt.substring(0, 128) + "...";
        String msg = comment.getUser().getFirstname() + " " + comment.getUser().getName() +" hat auf deinen Kommentar zu \"" +comment.getBill().getName()+
                "\"  geantwortet: \"" + cmttxt + "\"";
        sendNotification(comment.getReply_comment().getUser().getId(), msg);
    }

    //sende wenn ein Kommentar zum ersten Mal von einem verifizierten Nutzer bewertet wird
    //erstmal nicht...
    public void sendLikeNotificationBundle(List<Comment> comments){
        List<Notification> result = new ArrayList<>();
        for (Comment comment : comments) {
            String cmttxt = comment.getText().length() < 128 ? comment.getText() : comment.getText().substring(0, 128) + "...";
            String msg = "Dein Kommentar ist beliebt! \"" + cmttxt + "\"";
            Notification notification = new Notification();
            notification.setReceiver_user_id(comment.getUser().getId());
            notification.setMessage(msg);
            notification.setCreated_time(new Date());
            result.add(notification);
        }
        notificationRepository.saveAll(result);
    }

    //sende wenn Nutzer verifiziert wird
    public void sendVerifiedNotification(Long user_id){
        sendNotification(user_id, "Dein Account wurde verifiziert!");
    }

    //sende wenn Nutzer nicht verifiziert wird
    public void sendNotVerifiedNotification(Long user_id, String msg){
        sendNotification(user_id, msg);
    }



    public void sendReplyNotificationBundle(List<Comment> recentReplies) {
        List<Notification> result = new ArrayList<>();
        for(Comment comment : recentReplies){
            String cmttxt = htmlConverterService.htmlToString(comment.getText());
            cmttxt = comment.getText().length() < 128 ? cmttxt : cmttxt.substring(0, 128) + "...";
            String msg = comment.getUser().getFirstname() + " " + comment.getUser().getName() +" hat auf deinen Kommentar zu \"" +comment.getBill().getName()+
                    "\"  geantwortet: \"" + cmttxt + "\"";
            Notification notification = new Notification();
            notification.setReceiver_user_id(comment.getReply_comment().getUser().getId());
            notification.setMessage(msg);
            notification.setCreated_time(new Date());
            result.add(notification);
        }
        notificationRepository.saveAll(result);
    }

    //sende wenn Abstimmungsergebnisse der Abgeordneten eingehen
    public void sendVoteResultNotificationBundle(List<Long> user_ids, Bill bill) {
        List<Notification> result = new ArrayList<>();
        for(Long user_id : user_ids){
            String msg = "Finale Abstimmungsergebnise für \"" + bill.getName() + "\" sind eingetroffen!";
            Notification notification = new Notification();
            notification.setReceiver_user_id(user_id);
            notification.setMessage(msg);
            notification.setCreated_time(new Date());
            result.add(notification);
        }
        notificationRepository.saveAll(result);
    }

    public void markReadNotifications(List<Long> readNotificationsIds) {
        notificationRepository.markReadNotifications(readNotificationsIds);
    }
}
