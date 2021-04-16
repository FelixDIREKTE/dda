package com.dd.dda.controller;

import com.dd.dda.model.sqldata.Comment;
import com.dd.dda.model.sqldata.User;
import com.dd.dda.service.CommentRatingService;
import com.dd.dda.service.CommentService;
import com.dd.dda.service.MailService;
import com.dd.dda.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;
    private final MailService mailService;
    private final UserService userService;
    private final CommentRatingService commentRatingService;

    public CommentController(CommentService commentService, MailService mailService, UserService userService, CommentRatingService commentRatingService) {
        this.commentService = commentService;
        this.mailService = mailService;
        this.userService = userService;
        this.commentRatingService = commentRatingService;
    }


    @PutMapping("/{id}/create")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<Comment> createComment(    @PathVariable(value = "id") Long id,
                                                    @RequestParam(value = "text") String text,
                                                    @RequestParam(value = "bill_id") Long bill_id,
                                                     @RequestParam(value = "replied_comment_id") Long replied_comment_id
                                                     //@RequestParam(value = "pro") Boolean pro

    ) {
        log.info("Enter into comment");
        if (text == null || text.isEmpty()) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        }
        if( bill_id == null || replied_comment_id == null){
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        }
        if(replied_comment_id == -1) {
            Comment result = commentService.createComment(id, bill_id, text, null, true);
            return ResponseEntity.ok(result);
        } else if (replied_comment_id == -2){
            Comment result = commentService.createComment(id, bill_id, text, null, false);
            return ResponseEntity.ok(result);
        } else {
            Comment result = commentService.createComment(id, bill_id, text, replied_comment_id, true);
            return ResponseEntity.ok(result);
        }
    }


    /*@PutMapping("/{id}/updateData")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<String> update(@PathVariable(value = "id") Long id,
                                         @RequestParam(value = "comment_id") Long comment_id,
                                         @RequestParam(value = "text") String text
    ) {
        log.info("Enter into updateData");
        String result = commentService.update(id, comment_id, text);
        return ResponseEntity.ok(result);
    }*/






    @GetMapping("{id}/getRankedComments")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<List<Comment>> getRankedComments(@PathVariable(value = "id") Long id,
                                                           @RequestParam(value = "bill_id") Long bill_id,
                                                           @RequestParam(value = "reply_comment_id") Long reply_comment_id
                                                           ) {
        log.info("enter getBills " + id);
        List<Comment> result = commentService.getRankedComments(id, bill_id, reply_comment_id);
        return ResponseEntity.ok(result);

    }


    @DeleteMapping("{id}/delete")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<Boolean> delete(@PathVariable(value = "id") Long id,
                                          @RequestParam(value = "comment_id") Long comment_id)  {
        commentService.delete(id, comment_id);

        return ResponseEntity.ok(true);
    }


    @PutMapping("/{id}/reportComment")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity reportComment(    @PathVariable(value = "id") Long id,
                                                     @RequestParam(value = "comment_id") Long comment_id
    ) {

        User reporter = userService.getUserByIdUnencrypted(id);
        Comment reportedComment = commentService.getCommentById(comment_id);
        mailService.reportComment(reporter, reportedComment);
        return ResponseEntity.ok(true);
    }


    @PutMapping("/{id}/saveReadComments")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity saveReadComments(    @PathVariable(value = "id") Long id,
                                            @RequestParam(value = "readCommentsIds") Long[] readCommentsIds  ) {

        userService.addCommentsRead(id, readCommentsIds.length);
        commentService.addReads(readCommentsIds);
        return ResponseEntity.ok(true);
    }

    @GetMapping("{id}/loadReadCommentsIds")
    @PreAuthorize("hasAuthority('User') and principal.id == #id")
    public ResponseEntity<List<Long>> loadReadCommentsIds(@PathVariable(value = "id") Long id,
                                                           @RequestParam(value = "bill_id") Long bill_id
    ) {
        log.info("enter getBills " + id);
        List<Long> ratedcommentsids = commentService.getCommentIdsOfRatingsForBill(id, bill_id);
        return ResponseEntity.ok(ratedcommentsids);
    }


}
