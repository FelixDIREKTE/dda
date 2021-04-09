package com.dd.dda.controller;

import com.dd.dda.model.sqldata.Notification;
import com.dd.dda.service.NotificationService;
import com.dd.dda.service.UtilService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UtilService utilService;

    public NotificationController(NotificationService notificationService, UtilService utilService) {
        this.notificationService = notificationService;
        this.utilService = utilService;
    }

    @GetMapping("{id}/getNotifications")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<List<Notification>> getNotifications(@PathVariable(value = "id") Long id
    ) {
        log.info("get Notifications");
        List<Notification> result = notificationService.getNotifications(id);
        return ResponseEntity.ok(result);
    }

    @PutMapping("{id}/markReadNotifications")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity markReadNotifications(@PathVariable(value = "id") Long id,
                                                @RequestParam(value = "readNotificationsIds") String readNotificationsIds
    ) {
        List<Long> ids = utilService.stringToArray(readNotificationsIds);
        notificationService.markReadNotifications(ids);
        return ResponseEntity.ok(true);
    }


    //
}
