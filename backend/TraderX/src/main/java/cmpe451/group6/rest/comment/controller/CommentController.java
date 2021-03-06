package cmpe451.group6.rest.comment.controller;

import cmpe451.group6.Util;
import cmpe451.group6.authorization.dto.StringResponseWrapper;
import cmpe451.group6.authorization.exception.CustomException;
import cmpe451.group6.rest.comment.model.CommentRequestDTO;
import cmpe451.group6.rest.comment.model.article.ArticleCommentResponseDTO;
import cmpe451.group6.rest.comment.model.equipment.EquipmentCommentResponseDTO;
import cmpe451.group6.rest.comment.service.article.ArticleCommentService;
import cmpe451.group6.rest.comment.service.equipment.EquipmentCommentService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/comment")
@Api(tags = "Comment")
public class CommentController {

    @Autowired
    EquipmentCommentService equipmentCommentService;

    @Autowired
    ArticleCommentService articleCommentService;

    @Autowired
    Util util;

    @PostMapping(value = "/article/vote/{id}/{type}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_BASIC') or hasRole('ROLE_TRADER')")
    @ApiOperation(value = "Vote article comment", response = StringResponseWrapper.class)
    @ApiResponses(value = {
            @ApiResponse(code = 406, message = "Cannot vote own comments."),
            @ApiResponse(code = 409, message = "Comment has already been voted."),
            @ApiResponse(code = 412, message = "No such comment"),
            @ApiResponse(code = 422, message = "Invalid vote type.")})
    public StringResponseWrapper voteArticleComment(@ApiParam("Comment id") @PathVariable("id") String id,
                                             @ApiParam("Vote type. [ up | down]") @PathVariable String type,
                                             HttpServletRequest req) {
        boolean isUpvote = verifyPathParams(id,type);
        return new StringResponseWrapper(articleCommentService.vote(util.unwrapUsername(req),Integer.parseInt(id),isUpvote));
    }

    @DeleteMapping(value = "/article/revoke/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_BASIC') or hasRole('ROLE_TRADER')")
    @ApiOperation(value = "Revoke previously made article vote.", response = StringResponseWrapper.class)
    @ApiResponses(value = {
            @ApiResponse(code = 406, message = "Cannot vote own comments."),
            @ApiResponse(code = 409, message = "Comment has not been voted."),
            @ApiResponse(code = 412, message = "No such comment")})
    public StringResponseWrapper revokeArticleCommentVote(@ApiParam("Comment id") @PathVariable("id") String id,
                                                   HttpServletRequest req) {
        verifyPathParams(id,"up"); // type is not necessary here. only id check is performed.
        return new StringResponseWrapper(articleCommentService.revokeVote(util.unwrapUsername(req),Integer.parseInt(id)));
    }

    @PostMapping(value = "/article/post/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_BASIC') or hasRole('ROLE_TRADER')")
    @ApiOperation(value = "Post comment on article (1-1000 chars)", response = EquipmentCommentResponseDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 406, message = "No such an article/user found or improper length.")})
    public ArticleCommentResponseDTO postArticleComment(@ApiParam("Article Id") @PathVariable String id,
                                                 @ApiParam("Comment") @RequestBody CommentRequestDTO comment, HttpServletRequest req) {
        verifyPathParams(id,"up"); // up is ignored, only perform id check.
        return articleCommentService.postArticleComment(util.unwrapUsername(req),comment.getComment(),Integer.parseInt(id));
    }

    @DeleteMapping(value = "/article/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_BASIC') or hasRole('ROLE_TRADER')")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Delete article comment", response = StringResponseWrapper.class)
    @ApiResponses(value = {
            @ApiResponse(code = 406, message = "Cannot delete other's comment."),
            @ApiResponse(code = 412, message = "No such an comment found.")})
    public StringResponseWrapper deleteArticleComment(@ApiParam("Equipment Code") @PathVariable String id,
                                               HttpServletRequest req) {
        verifyPathParams(id,"up"); // up is ignored, only perform id check.
        articleCommentService.deleteArticleComment(util.unwrapUsername(req),Integer.parseInt(id));
        return new StringResponseWrapper("Comment has been deleted");
    }


    @PostMapping(value = "/article/edit/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_BASIC') or hasRole('ROLE_TRADER')")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Edit article comment. Response is the updated content", response = StringResponseWrapper.class)
    @ApiResponses(value = {
            @ApiResponse(code = 406, message = "Cannot edit other's comment."),
            @ApiResponse(code = 412, message = "No such an article/user found or improper length.")})
    public StringResponseWrapper editArticleComment(@ApiParam("Article id") @PathVariable String id,
                                             @ApiParam("Comment") @RequestBody CommentRequestDTO comment,
                                             HttpServletRequest req) {
        verifyPathParams(id,"up"); // up is ignored, only perform id check.
        articleCommentService.editArticleComment(util.unwrapUsername(req),Integer.parseInt(id),comment.getComment());
        return new StringResponseWrapper(comment.getComment());
    }

    @GetMapping(value = "/article/{username}/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_BASIC') or hasRole('ROLE_TRADER')")
    @ApiOperation(value = "Find comments by username and article id", response = EquipmentCommentResponseDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 412, message = "No such an article/user found"),
            @ApiResponse(code = 428, message = "Profile is private.")})
    public List<ArticleCommentResponseDTO> findByUsernameAndCodeArticles(
            @ApiParam("Username") @PathVariable String username,
            @ApiParam("Article Id") @PathVariable String id,
            HttpServletRequest req) {
        verifyPathParams(id,"up");
        return articleCommentService.findArticleCommentsByUser(util.unwrapUsername(req),username,Integer.parseInt(id));
    }

    @GetMapping(value = "/article/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Find article comments", response = ArticleCommentResponseDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 412, message = "No such an article found")})
    public List<ArticleCommentResponseDTO> findArticleComments(@ApiParam("Article Id") @PathVariable String id,
                                                                   HttpServletRequest req) {
        verifyPathParams(id,"up");
        return articleCommentService.findArticleComments(Integer.parseInt(id),util.unwrapUsername(req));
    }

    @GetMapping(value = "/user/article/{username}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_BASIC') or hasRole('ROLE_TRADER')")
    @ApiOperation(value = "Find user comments on articles.", response = ArticleCommentResponseDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 428, message = "Profile is private"),
            @ApiResponse(code = 412, message = "No such user is found")})
    public List<ArticleCommentResponseDTO> findUserArticleComments(@ApiParam("Username")
                                                     @PathVariable String username,
                                                              HttpServletRequest req) {
        return articleCommentService.findUserComments(username,util.unwrapUsername(req));
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~``
    // Equipment Comments

    @PostMapping(value = "/equipment/vote/{id}/{type}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_BASIC') or hasRole('ROLE_TRADER')")
    @ApiOperation(value = "Vote comment", response = StringResponseWrapper.class)
    @ApiResponses(value = {
            @ApiResponse(code = 406, message = "Cannot vote own comments."),
            @ApiResponse(code = 409, message = "Comment has already been voted."),
            @ApiResponse(code = 412, message = "No such comment"),
            @ApiResponse(code = 422, message = "Invalid vote type.")})
    public StringResponseWrapper voteComment(@ApiParam("Comment id") @PathVariable("id") String id,
                                             @ApiParam("Vote type. [ up | down]") @PathVariable String type,
                                             HttpServletRequest req) {
        boolean isUpvote = verifyPathParams(id,type);
        return new StringResponseWrapper(equipmentCommentService.vote(util.unwrapUsername(req),Integer.parseInt(id),isUpvote));
    }

    @DeleteMapping(value = "/equipment/revoke/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_BASIC') or hasRole('ROLE_TRADER')")
    @ApiOperation(value = "Revoke previously made vote.", response = StringResponseWrapper.class)
    @ApiResponses(value = {
            @ApiResponse(code = 406, message = "Cannot vote own comments."),
            @ApiResponse(code = 409, message = "Comment has not been voted."),
            @ApiResponse(code = 412, message = "No such comment")})
    public StringResponseWrapper revokeCommentVote(@ApiParam("Comment id") @PathVariable("id") String id,
                                             HttpServletRequest req) {
        verifyPathParams(id,"up"); // type is not necessary here. only id check is performed.
        return new StringResponseWrapper(equipmentCommentService.revokeVote(util.unwrapUsername(req),Integer.parseInt(id)));
    }

    @PostMapping(value = "/equipment/post/{code}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_BASIC') or hasRole('ROLE_TRADER')")
    @ApiOperation(value = "Post comment on equipment (1-1000 chars)", response = EquipmentCommentResponseDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 406, message = "No such an equipment/user found or improper length.")})
    public EquipmentCommentResponseDTO postComment(@ApiParam("Equipment Code") @PathVariable String code,
                                                   @ApiParam("Comment") @RequestBody CommentRequestDTO comment, HttpServletRequest req) {
        return equipmentCommentService.postEquipmentComment(util.unwrapUsername(req),comment.getComment(),code);
    }

    @DeleteMapping(value = "/equipment/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_BASIC') or hasRole('ROLE_TRADER')")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Delete equipment comment", response = StringResponseWrapper.class)
    @ApiResponses(value = {
            @ApiResponse(code = 406, message = "Cannot delete other's comment."),
            @ApiResponse(code = 412, message = "No such an comment found.")})
    public StringResponseWrapper deleteComment(@ApiParam("Equipment Code") @PathVariable int id,
                                               HttpServletRequest req) {
        equipmentCommentService.deleteEquipmentComment(util.unwrapUsername(req),id);
        return new StringResponseWrapper("Comment has been deleted");
    }


    @PostMapping(value = "/equipment/edit/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_BASIC') or hasRole('ROLE_TRADER')")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Edit comment. Response is the updated content", response = StringResponseWrapper.class)
    @ApiResponses(value = {
            @ApiResponse(code = 406, message = "Cannot edit other's comment."),
            @ApiResponse(code = 412, message = "No such an equipment/user found or improper length.")})
    public StringResponseWrapper editComment(@ApiParam("Equipment Code") @PathVariable int id,
                                             @ApiParam("Comment") @RequestBody CommentRequestDTO comment,
                                               HttpServletRequest req) {
        equipmentCommentService.editEquipmentComment(util.unwrapUsername(req),id,comment.getComment());
        return new StringResponseWrapper(comment.getComment());
    }

    @GetMapping(value = "/equipment/{username}/{code}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_BASIC') or hasRole('ROLE_TRADER')")
    @ApiOperation(value = "Find comments by username and code", response = EquipmentCommentResponseDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 412, message = "No such an equipment/user found"),
            @ApiResponse(code = 428, message = "Profile is private.")})
    public List<EquipmentCommentResponseDTO> findByUsernameAndCode(
                            @ApiParam("Username") @PathVariable String username,
                            @ApiParam("Equipment Code") @PathVariable String code,
                            HttpServletRequest req) {
        return equipmentCommentService.findEquipmentCommentsByUser(util.unwrapUsername(req),username,code);
    }

    @GetMapping(value = "/equipment/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Find equipment comments", response = EquipmentCommentResponseDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 412, message = "No such an equipment found")})
    public List<EquipmentCommentResponseDTO> findEquipmentComments(@ApiParam("Equipment Code") @PathVariable String code,
                                                                   HttpServletRequest req) {
        return equipmentCommentService.findEquipmentComments(code,util.unwrapUsername(req));
    }

    @GetMapping(value = "/user/equipment/{username}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_BASIC') or hasRole('ROLE_TRADER')")
    @ApiOperation(value = "Find user comments for equipments", response = EquipmentCommentResponseDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 428, message = "Profile is private"),
            @ApiResponse(code = 412, message = "No such user is found")})
    public List<EquipmentCommentResponseDTO> findUserComments(@ApiParam("Username")
                                                       @PathVariable String username,
                                                              HttpServletRequest req) {
        return equipmentCommentService.findUserComments(username,util.unwrapUsername(req));
    }

    private boolean verifyPathParams(String id, String type){
        try {
            Integer.parseInt(id);
        } catch (NumberFormatException e) {
            throw new CustomException("Invalid id. Use integer values only",HttpStatus.UNPROCESSABLE_ENTITY);
        }
        Boolean isUpvote = null;
        if (type.equals("up")) isUpvote = true;
        if (type.equals("down")) isUpvote = false;
        if (isUpvote == null) throw new CustomException(
                String.format("Invalid vote type: %s. Use \"up\" or \"down\" only", type), HttpStatus.UNPROCESSABLE_ENTITY);
        return isUpvote;
    }


}
