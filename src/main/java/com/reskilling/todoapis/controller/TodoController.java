package com.reskilling.todoapis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.reskilling.todoapis.model.Todo;
import com.reskilling.todoapis.model.User;
import com.reskilling.todoapis.service.TodoService;
import com.reskilling.todoapis.service.UserService;
import com.reskilling.todoapis.utils.JwtUtility;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class TodoController {
	
	@Autowired
	TodoService todoService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	JwtUtility jwtUtils;
	
	private String username;
	
	@PostMapping("/todo/create")
	public ResponseEntity<?> createTodo(HttpServletRequest request, @RequestBody Todo todo) {
		System.out.println("todo from body= "+todo.toString());
		username=jwtUtils.extractUsername(request.getHeader("Authorization").substring(7));
		System.out.println("username extracted="+username);
		User user=userService.loadUserByUsername(username);
		System.out.println("user extracted="+user.toString());
		todo.setUser(user);
		if(todoService.createTodo(todo)) 
			return ResponseEntity.status(HttpStatus.CREATED).body("Todo created successfully");
		return ResponseEntity.internalServerError().body("Unable to create todo");
	}
	
	@DeleteMapping("/todo/delete/{todoId}")
	public ResponseEntity<?> deleteTodo(HttpServletRequest request, @PathVariable Long todoId){
		if(todoId!=null && todoService.retrieveTodoById(todoId)!=null)
		{
			todoService.deleteTodoByTid(todoId);
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.badRequest().body("Unexisting or invalid todo id");
	}

}
