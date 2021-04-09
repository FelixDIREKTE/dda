package com.dd.dda.service;

import com.dd.dda.model.FileType;
import com.dd.dda.model.sqldata.User;
import com.dd.dda.model.VerificationData;
import com.dd.dda.repository.UserRepository;
import com.dd.dda.model.VerificationStatus;
import com.dd.dda.model.exception.DDAException;
import com.dd.dda.service.file.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
public class VerificationService {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final DuplicateService duplicateService;
    private final MailService mailService;
    private final NotificationService notificationService;
    private final CommentService commentService;

    public VerificationService(UserRepository userRepository, FileStorageService fileStorageService, DuplicateService duplicateService, MailService mailService, NotificationService notificationService, CommentService commentService) {
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.duplicateService = duplicateService;
        this.mailService = mailService;
        this.notificationService = notificationService;
        this.commentService = commentService;
    }

    public VerificationData getWaitingUser() {
        int nrWaiting = userRepository.nrOfWaitingUsers();
        User waitingUser = userRepository.waitingUser();
        VerificationData result = new VerificationData();
        if(waitingUser == null){
            nrWaiting = userRepository.nrOfWaitingUsers2();
            waitingUser = userRepository.waitingUser2();

        }
        if(waitingUser != null){
            waitingUser.setVerificationstatus(VerificationStatus.LOCKEDBYADMIN);
            userRepository.save(waitingUser);
            result.duplicats = duplicateService.searchDuplicates(waitingUser);
        }
        result.waitingUsers = nrWaiting;
        result.userToVerify = waitingUser;
        return result;

    }

    public String verify(Long verifiedId, Long id, boolean verify, String msg) {
        Optional<User> verifiedUserO = userRepository.findById(verifiedId);
        if(verifiedUserO.isEmpty()){
            throw new DDAException("Id für zu verifizierenden Nutzer nicht vorhanden: " + verifiedId);
        }
        User verifiedUser = verifiedUserO.get();
        if(verify){
            verifiedUser.setVerificationstatus(VerificationStatus.VERIFIED);
            verifiedUser.setVerifiedById(id);
            verifiedUser.setVerifiedDate(new Date());
            fileStorageService.deleteAllFiles(FileType.USERVERIFICATION, verifiedId);
            mailService.sendVerificationPositiveMessage(verifiedUser);
            notificationService.sendVerifiedNotification(verifiedId);
            commentService.sendNotificationsForRecentReplies(verifiedId);
        } else {
            verifiedUser.setVerificationstatus(VerificationStatus.DATANEEDED);
            mailService.sendVerificationNegativeMessage(verifiedUser, msg);
            notificationService.sendNotVerifiedNotification(verifiedId, msg);
        }
        userRepository.save(verifiedUser);
        return "ok";
    }

    public void handleDuplicate(Long verifiedId) {
        User fraud = userRepository.findById(verifiedId).get();
        fraud.setActive(false);
        fraud.setVerificationstatus(VerificationStatus.DATANEEDED);
        userRepository.save(fraud);
        mailService.sendDuplicateWarning(fraud);
    }
}
