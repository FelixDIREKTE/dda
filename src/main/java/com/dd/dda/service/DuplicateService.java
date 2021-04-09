package com.dd.dda.service;

import com.dd.dda.model.VerificationStatus;
import com.dd.dda.model.sqldata.User;
import com.dd.dda.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DuplicateService {

    private final UserRepository userRepository;
    private final MailService mailService;


    public DuplicateService(UserRepository userRepository, MailService mailService) {
        this.userRepository = userRepository;
        this.mailService = mailService;
    }

    public void searchDuplicates(){
        Map<Date, List<User>> usersByBd = userRepository.findAll().stream().filter(u -> u.getVerificationstatus() == VerificationStatus.LOCKEDBYADMIN || u.getVerificationstatus() == VerificationStatus.VERIFIED).collect(Collectors.groupingBy(User::getBirthdate));
        for(List<User> users : usersByBd.values()){
            for(int i = 0; i < users.size(); i++){
                for(int j = i+1; j < users.size(); j++){
                    if( compare(users.get(i), users.get(j))){
                        mailService.sendDuplicateInfo(users.get(i), users.get(j));
                        //reportPossibleDuplicate(users.get(i), users.get(j));
                    };
                }
            }
        }
    }


    private boolean compare(User user1, User user2) {
        boolean sameZipcode = user1.getZipcode().equals(user2.getZipcode());
        boolean samePW = user1.getPasswordHash().equals(user2.getPasswordHash());
        boolean sameFirstName = getFirstName(user1.getFirstname()).equals(getFirstName(user2.getFirstname()));
        boolean sameSurName = getFirstName(user1.getName()).equals(getFirstName(user2.getName()));

        int sameCnt = 0;
        if(sameZipcode){
            sameCnt++;
        }
        if(sameFirstName){
            sameCnt++;
        }
        if(sameSurName){
            sameCnt++;
        }

        if(samePW){
            sameCnt+=3;
        }
        if(sameCnt >= 2){
            return true;
        }

        return false;

    }

    private String getFirstName(String s){
        return s.split(" ")[0].split("-")[0].split(";")[0].split(",")[0].toLowerCase();
    }

    public List<User> searchDuplicates(User verifiedUser) {
        /*
        List<User> users = userRepository.findAll().stream().filter(
                u ->( u.getVerificationstatus().equals(VerificationStatus.LOCKEDBYADMIN)
                        || u.getVerificationstatus().equals(VerificationStatus.VERIFIED))
                && verifiedUser.getBirthdate().equals(u.getBirthdate())
                && !verifiedUser.getId().equals(u.getId())
        ).collect(Collectors.toList());

        for(User u : users){
            if( compare(u, verifiedUser)){
                mailService.sendDuplicateInfo(u, verifiedUser);
            };
        }*/

        List<User> result = userRepository.findAll().stream().filter(
                    u -> verifiedUser.getBirthdate().equals(u.getBirthdate()) &&
                        ( u.getVerificationstatus().equals(VerificationStatus.LOCKEDBYADMIN)
                        || u.getVerificationstatus().equals(VerificationStatus.VERIFIED))
                        && verifiedUser.getId() != u.getId()
                        && compare(u, verifiedUser)
           ).collect(Collectors.toList());

        /*result.stream().forEach(u -> {
               mailService.sendDuplicateInfo(u, verifiedUser);
           });*/
        return result;

    }
}
