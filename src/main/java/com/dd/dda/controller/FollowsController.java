package com.dd.dda.controller;

import com.dd.dda.service.FollowsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/follows")
public class FollowsController {

    private final FollowsService followsService;

    public FollowsController(FollowsService followsService) {
        this.followsService = followsService;
    }


    @GetMapping("{id}/getFollowingIds")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<List<Long>> getFollowingIds(@PathVariable(value = "id") Long id
                                                         ) {
        List<Long> result = followsService.getFollowindgIds(id);
        return ResponseEntity.ok(result);
    }

    @PutMapping("{id}/toggleFollow")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity toggleFollow(@PathVariable(value = "id") Long id,
                                                         @RequestParam(value = "idToFollow") Long idToFollow
                                                         ) {
        followsService.toggleFollow(id, idToFollow);
        return ResponseEntity.ok(true);
    }




}
