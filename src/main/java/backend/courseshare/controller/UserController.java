package backend.courseshare.controller;


import backend.courseshare.dto.user.UserPatchDTO;
import backend.courseshare.dto.user.UserProfileDTO;
import backend.courseshare.dto.user.UserUpdateDTO;
import backend.courseshare.security.CustomUserDetails;
import backend.courseshare.service.Userservice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    private final Userservice userservice;

    public UserController(Userservice userservice) {
        this.userservice = userservice;
    }

    //Get All Users
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserProfileDTO>> listAll() {
        List<UserProfileDTO> users = userservice.getAllUsers();
        return ResponseEntity.ok(users);
    }

    //Get A user by PublicId
    @GetMapping("/{publicId}")
    public ResponseEntity<UserProfileDTO> getByPublicId(
            @PathVariable String publicId,
            @AuthenticationPrincipal CustomUserDetails principal
            ) {
        //All Adminini and owner Allowed
        if(!isAdmin(principal) && !isOwner(principal,publicId)) {
            return ResponseEntity.status(403).build();
        }

        UserProfileDTO dto = userservice.getUserByPublicId(publicId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getMyProfile(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(
                userservice.getUserByPublicId(principal.getPublicId())
        );
    }



    //
    private boolean isAdmin(CustomUserDetails principal) {
        return principal != null && principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean isOwner(CustomUserDetails principal, String publicId) {
        return principal != null && publicId != null && publicId.equals(principal.getPublicId());
    }

    //PUT
    @PutMapping("/{publicId}")
    public ResponseEntity<UserProfileDTO> putByPublicId(
            @PathVariable String publicId,
            @Validated @RequestBody UserUpdateDTO updateDTO,
            @AuthenticationPrincipal CustomUserDetails principal
            ) {
        if (!isAdmin(principal) && !isOwner(principal,publicId)) {
            return ResponseEntity.status(403).build();
        }
        UserProfileDTO updated = userservice.updateUser(publicId,updateDTO);
        return ResponseEntity.ok(updated);
    }

    //Patch
    @PatchMapping("/{publicId}")
    public ResponseEntity<UserProfileDTO> patchByPublicId(
            @PathVariable String publicId,
            @Validated @RequestBody UserPatchDTO patchDTO,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (!isAdmin(principal) && !isOwner(principal, publicId)) {
            return ResponseEntity.status(403).build();
        }

        UserProfileDTO updated =userservice.patchUser(publicId, patchDTO);
        return ResponseEntity.ok(updated);
    }

    //DELETE
    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> deleteByPublicId(
            @PathVariable String publicId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if(!isAdmin(principal) || !isOwner(principal, publicId)) {
            return ResponseEntity.status(403).build();
        }

        userservice.deleteUserByPublicId(publicId);
        return ResponseEntity.noContent().build();
    }



}
