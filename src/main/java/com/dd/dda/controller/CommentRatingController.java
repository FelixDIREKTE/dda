package com.dd.dda.controller;

import com.dd.dda.model.CommentRatingEnum;
import com.dd.dda.model.sqldata.CommentRating;
import com.dd.dda.service.CommentRatingService;
import com.dd.dda.service.CommentService;
import com.dd.dda.service.UtilService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/commentrating")
public class CommentRatingController {

    private final CommentRatingService commentRatingService;
    private final CommentService commentService;
    private final UtilService utilService;

    public CommentRatingController(CommentRatingService commentRatingService, CommentService commentService, UtilService utilService) {
        this.commentRatingService = commentRatingService;
        this.commentService = commentService;
        this.utilService = utilService;
    }



    /*@GetMapping("/{id}/getVotes")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<Map<Object, Integer>> getVotes(@PathVariable(value = "id") Long id,
                                                                    @RequestParam(value = "comment_id") Long comment_id
    ) {
        log.info("Enter getVotes");
        Map<Object, Integer> result = commentRatingService.getVotes(comment_id);
        return ResponseEntity.ok(result);
    }*/

    @GetMapping("/{id}/getVotesAsString")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<String> getVotesAsString(@PathVariable(value = "id") Long id,
                                                                    @RequestParam(value = "comment_id") Long comment_id
    ) {
        log.info("Enter getVotes");
        Map<Object, Integer> result0 = commentRatingService.getVotes(comment_id);
        int repl = commentService.getNrOfReplies(comment_id);
        result0.put("REPLIES", repl);
        String result = utilService.mapToString(result0);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/{id}/getVotesAsStringBundle")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<List<String>> getVotesAsStringBundle(@PathVariable(value = "id") Long id,
                                                   @RequestParam(value = "comment_ids") String comment_ids
    ) {
        log.info("Enter getVotes");
        List<Long> ids = utilService.stringToArray(comment_ids);
        List<String> result = new ArrayList<>();

        List<Map<Object, Integer>> result0 = commentRatingService.getVotesBundle(ids);
        List<Integer> repl = commentService.getNrOfRepliesBundle(ids);
        for(int i = 0; i < ids.size(); i++){
            result0.get(i).put("REPLIES", repl.get(i));
            String resultPart = utilService.mapToString(result0.get(i));
            result.add(resultPart);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/getUserVote")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<CommentRating> getUserVote(@PathVariable(value = "id") Long id,
                                                     @RequestParam(value = "comment_id") Long comment_id
    ) {
        log.info("Enter getUserVote");
        CommentRating result = commentRatingService.getById(id, comment_id);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/{id}/getUserVoteBundle")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<List<CommentRating>> getUserVoteBundle(@PathVariable(value = "id") Long id,
                                                           @RequestParam(value = "comment_ids") String comment_ids

    ) {
        List<Long> ids = utilService.stringToArray(comment_ids);
        List<CommentRating> result = commentRatingService.getByIdBundle(id, ids);
        return ResponseEntity.ok(result);
    }




    @PutMapping("/{id}/vote")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<CommentRating> vote(@PathVariable(value = "id") Long id,
                                             @RequestParam(value = "comment_id") Long comment_id,
                                             @RequestParam(value = "vote") CommentRatingEnum vote
    ) {
        log.info("Enter vote");
        CommentRating result = commentRatingService.vote(id, comment_id, vote);
        return ResponseEntity.ok(result);

    }

    @DeleteMapping("/{id}/deleteVote")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<Boolean> deleteVote(@PathVariable(value = "id") Long id,
                                              @RequestParam(value = "comment_id") Long comment_id
    ) {
        log.info("Enter deleteVote");
        commentRatingService.deleteVote(id, comment_id);
        return ResponseEntity.ok(true);

    }

    @PutMapping("/{id}/pressVote")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<CommentRating> pressVote(@PathVariable(value = "id") Long id,
                                              @RequestParam(value = "comment_id") Long comment_id,
                                              @RequestParam(value = "vote") CommentRatingEnum vote
    ) {
        log.info("Enter pressVote");
        CommentRating result = commentRatingService.getById(id, comment_id);
        if(result == null) {
            result = commentRatingService.vote(id, comment_id, vote);
        } else {
            commentRatingService.deleteVote(id, comment_id);
            result = null;
        }
        return ResponseEntity.ok(result);

    }

}
