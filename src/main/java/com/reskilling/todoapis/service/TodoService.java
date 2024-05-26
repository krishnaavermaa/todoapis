package com.reskilling.todoapis.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.reskilling.todoapis.model.Todo;
import com.reskilling.todoapis.model.User;
import com.reskilling.todoapis.repository.TodoRepository;

@Service
public class TodoService {

	@Autowired
	TodoRepository repository;

	public boolean createTodo(Todo todo) {
		return repository.save(todo) != null;
	}

	public void deleteTodoByTid(Long todoId) {
		repository.deleteById(todoId);
	}

	public Todo retrieveTodoById(Long todoId) {
		Todo todo = repository.findByTodoId(todoId);
//		if (todo.retrieveUser()==user)
			return todo;
//		else
//			return null;
	}

	public boolean updateTodo(Long todoId, Todo updatedTodo) {
		Optional<Todo> op = repository.findById(todoId);
		Todo todo = null;
		if (op.isPresent())
			todo = op.get();
		if (todo != null) {
			if(updatedTodo.getTitle()!=null)
			todo.setTitle(updatedTodo.getTitle());
			if(updatedTodo.getDescription()!=null)
			todo.setDescription(updatedTodo.getDescription());
			if(updatedTodo.getPriority()!=null)
			todo.setPriority(updatedTodo.getPriority());
			if(updatedTodo.getEndDate()!=null)
			todo.setEndDate(updatedTodo.getEndDate());
			if(updatedTodo.isCompleted()!=null)
			todo.setCompleted(updatedTodo.isCompleted());
			try {
				repository.save(todo);
				return true;
			} catch (Exception e) {
			}
		}
		return false;

	}

	public boolean deleteAllTodo(User user) {
		List<Todo> todos = repository.findByUser(user);
		if (todos.size() > 0) {
			repository.deleteAll(todos);
			return true;
		} else
			return false;

	}

	public List<Todo> retrieveAllOverdues(User user) {
		List<Todo> todos = repository.findByUser(user).stream()
				.filter(todo-> !todo.isCompleted() && todo.getEndDate().isBefore(LocalDate.now()))
				.toList();
		return todos;
	}

	public List<Todo> retrieveAllTodayTodo(User user) {
		List<Todo> todos = repository.findByUser(user).stream()
				.filter(todo-> todo.getEndDate().isEqual(LocalDate.now()))
				.toList();
		return todos;
	}

	public List<Todo> retrieveAllTodo(User user) {
		List<Todo> todos = repository.findByUser(user);
		return todos;
	}

}
