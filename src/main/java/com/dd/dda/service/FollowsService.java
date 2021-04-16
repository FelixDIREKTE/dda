package com.dd.dda.service;

import com.dd.dda.model.VerificationStatus;
import com.dd.dda.model.idclasses.FollowsId;
import com.dd.dda.model.sqldata.Follows;
import com.dd.dda.model.sqldata.User;
import com.dd.dda.repository.FollowsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class FollowsService {

    private final FollowsRepository followsRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    public FollowsService(FollowsRepository followsRepository, UserService userService, NotificationService notificationService) {
        this.followsRepository = followsRepository;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    public List<Long> getFollowindgIds(Long id) {
        return followsRepository.getFollowingIds(id);
    }

    public void toggleFollow(Long id, Long idToFollow) {
        FollowsId followsId = new FollowsId(id, idToFollow);
        Optional<Follows> follows = followsRepository.findById(followsId);
        if(follows.isPresent()){
            followsRepository.delete(follows.get());
        } else {
            Follows f = new Follows();
            f.setFollower_id(id);
            f.setFollowee_id(idToFollow);
            followsRepository.save(f);

            User follower = userService.getUserByIdUnencrypted(id);
            User followee = userService.getUserByIdUnencrypted(idToFollow);
            if(follower.getVerificationstatus() == VerificationStatus.VERIFIED) {
                notificationService.sendFollowsNotification(follower, followee);
            }

        }
    }
}
