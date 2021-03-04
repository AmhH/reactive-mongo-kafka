package com.example.repository;

import com.example.model.Person;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface PersonRepository  extends ReactiveMongoRepository<Person, String> {

}
