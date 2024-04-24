package com.reskilling.todoapis.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.reskilling.todoapis.model.Todo;
import com.reskilling.todoapis.repository.TodoRepository;

@Service
public class TodoService {
	
	@Autowired
	TodoRepository repository;
	
	public boolean createTodo(Todo todo)
	{
		return repository.save(todo) != null;
	}
	
	public void deleteTodoByTid(Long todoId)
	{
		repository.deleteById(todoId);
	}
	
	public Todo retrieveTodoById(Long todoId)
	{
		Optional<Todo> op= repository.findById(todoId);
		if(op.isPresent()) return op.get();
		else return null;
	}

}
