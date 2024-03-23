package org.gauravagrwl.myApp.model.repositories;

import org.gauravagrwl.myApp.model.profile.ProfileDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;


public interface ProfileDocumentRepository extends MongoRepository<ProfileDocument, String> {

    public boolean existsByUserName(String userName);

   public List<ProfileDocument> findByUserName(String userName);

}
