package org.gauravagrwl.myApp.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.gauravagrwl.myApp.model.profile.ProfileDocument;
import org.gauravagrwl.myApp.service.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    ProfileService profileService;

    Logger LOGGER = LoggerFactory.getLogger(ProfileController.class);

    /**
     * 
     * @param profileService
     */
    ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * 
     * @param profile
     * @return
     */
    @PostMapping(value = "/addProfile", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> postMethodName(@RequestBody ProfileDocument profile) {
        return ResponseEntity.ok(profileService.saveProfileDocument(profile));
    }

    /**
     * 
     * @param userName
     * @return
     */
    @GetMapping(value = "/getProfile", produces = "application/json")
    public ResponseEntity<ProfileDocument> getMethodName(@RequestParam String userName) {
        return ResponseEntity.ok(profileService.getProfileDocument(userName));
    }

    /**
     * 
     * @return
     */
    @DeleteMapping("/dropAllCollections")
    public ResponseEntity<String> dropAllCollections() {
        LOGGER.info("Dropping the database.");
        return ResponseEntity.ok(profileService.dropDatabase());
    }

}
