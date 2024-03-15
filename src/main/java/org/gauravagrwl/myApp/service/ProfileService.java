package org.gauravagrwl.myApp.service;

import java.util.List;

import org.gauravagrwl.myApp.exception.AppException;
import org.gauravagrwl.myApp.model.ProfileDocument;
import org.gauravagrwl.myApp.model.repositories.ProfileDocumentRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private ProfileDocumentRepository profileDocumentRepository;

    private MongoTemplate template;

    ProfileService(ProfileDocumentRepository profileDocumentRepository, MongoTemplate template) {
        this.profileDocumentRepository = profileDocumentRepository;
        this.template = template;
    }

    public String saveProfileDocument(ProfileDocument document) {
        if (profileDocumentRepository.existsByUserName(document.getUserName()))
            throw new AppException("User already exist!");
        ProfileDocument insert = profileDocumentRepository.insert(document);
        return ("Document Inserted with id: " + insert.getId());
    }

    public ProfileDocument getProfileDocument(String userName) {
        List<ProfileDocument> byUserName = profileDocumentRepository.findByUserName(userName);
        if (byUserName.size() != 1) {
            throw new AppException("No user found!");
        }
        return byUserName.get(0);
    }

    public List<ProfileDocument> getAllProfileDocument() {
        List<ProfileDocument> allUserProfile = profileDocumentRepository.findAll();
        if (allUserProfile.size() != 1) {
            throw new AppException("No user found!");
        }
        return allUserProfile;
    }

    public String dropDatabase() {
        template.getCollectionNames().stream().forEach(col -> template.dropCollection(col));
        return "Warning All Database is dropped";

    }

}
