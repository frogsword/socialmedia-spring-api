package com.app.socialmedia.repositories;

import com.app.socialmedia.models.Tweet;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TweetRepository extends MongoRepository<Tweet, String> {
}
