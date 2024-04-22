package com.reskilling.todoapis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.reskilling.todoapis.model.Todo;
import com.reskilling.todoapis.model.User;

@Repository
public interface TodoRepository extends JpaRepository<Todo	, Long>{
	
	List<Todo> findByUser(User user);

}
