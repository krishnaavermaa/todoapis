package com.reskilling.todoapis.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.reskilling.todoapis.model.Todo;
import com.reskilling.todoapis.model.User;
import com.reskilling.todoapis.service.TodoService;
import com.reskilling.todoapis.service.UserService;
import com.reskilling.todoapis.utils.JwtUtility;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

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
	public ResponseEntity<?> createTodo(HttpServletRequest request,@Valid @RequestBody Todo todo, BindingResult bindingResult) {
		if(bindingResult.hasErrors()) {
			Map<String,String> errors=new HashMap<>();
			bindingResult.getFieldErrors()
			.forEach(err->
					errors.put(err.getField(), err.getDefaultMessage()));
			return ResponseEntity.badRequest().body(errors);
		}
		System.out.println("todo from body= " + todo.toString());
		username = jwtUtils.extractUsername(request.getHeader("Authorization").substring(7));
		System.out.println("username extracted=" + username);
		User user = userService.loadUserByUsername(username);
		System.out.println("user extracted=" + user.toString());
		todo.setUser(user);
		if(todo.isCompleted()==null) todo.setCompleted(false);
		if (todoService.createTodo(todo))
			return ResponseEntity.status(HttpStatus.CREATED).body("Todo created successfully");
		return ResponseEntity.internalServerError().body("Unable to create todo");
	}

	@DeleteMapping("/todo/delete/{todoId}")
	public ResponseEntity<?> deleteTodo(HttpServletRequest request, @PathVariable Long todoId) {
		username = jwtUtils.extractUsername(request.getHeader("Authorization").substring(7));
		User user = userService.loadUserByUsername(username);
		Todo retrievedTodo = todoService.retrieveTodoById(todoId);
		if (todoId != null) {
			if (retrievedTodo == null)
				return ResponseEntity.notFound().build();
			else if (retrievedTodo.retrieveUser().getId() != user.getId()) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body("You do not have permission to modify this todo");
			} else {
				todoService.deleteTodoByTid(todoId);
				return ResponseEntity.noContent().build();
			}
		} else
			return ResponseEntity.badRequest().body("Unexisting or invalid todo id");
	}

	@DeleteMapping("/todo/deleteAll")
	public ResponseEntity<?> deleteAllTodo(HttpServletRequest request, @RequestParam("confirm") Boolean confirm) {
		username = jwtUtils.extractUsername(request.getHeader("Authorization").substring(7));
		User user = userService.loadUserByUsername(username);
		if(confirm) {
			if(todoService.deleteAllTodo(user))
				return ResponseEntity.noContent().build();
			else return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("No Todo present!");
		}
		else return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("The 'confirm' query parameter must be true to proceed with the request!");
	}

	@PutMapping("/todo/update/{todoId}")
	public ResponseEntity<?> updateTodo(HttpServletRequest request, @PathVariable Long todoId,
			@RequestBody Todo updatedTodo) {
		username = jwtUtils.extractUsername(request.getHeader("Authorization").substring(7));
		User user = userService.loadUserByUsername(username);
		Todo retrievedTodo = todoService.retrieveTodoById(todoId);
		if (todoId != null) {
			if (retrievedTodo == null)
				return ResponseEntity.notFound().build();
			else if (retrievedTodo.retrieveUser().getId() != user.getId()) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body("You do not have permission to modify this todo");
			} else {
				if (todoService.updateTodo(todoId, updatedTodo))
					return ResponseEntity.ok("Todo updated successfully");
				else
					return ResponseEntity.internalServerError()
							.body("Some internal error occurred while updating todo");
			}
		} else
			return ResponseEntity.badRequest().body("Unexisting or invalid todo id");
	}

	@GetMapping("/todo/{todoId}")
	public ResponseEntity<?> retrieveTodo(HttpServletRequest request, @PathVariable Long todoId) {
		username = jwtUtils.extractUsername(request.getHeader("Authorization").substring(7));
		User user = userService.loadUserByUsername(username);
		Todo retrievedTodo = null;
		if (todoId != null) {
			retrievedTodo = todoService.retrieveTodoById(todoId);
			if (retrievedTodo == null)
				return ResponseEntity.notFound().build();
			else if (retrievedTodo.retrieveUser().getId() != user.getId()) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body("You do not have permission to view this todo");
			} else {
				return ResponseEntity.ok(retrievedTodo);
			}
		} else
			return ResponseEntity.badRequest().body("Unexisting or invalid todo id");
	}

	@GetMapping("/todo")
	public ResponseEntity<?> retrieveTodo(HttpServletRequest request,
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "completed", required = false) Boolean completed,
			@RequestParam(name = "enddate", required = false) LocalDate endDate,
			@RequestParam(name = "priority", required = false) Long priority) {
		username = jwtUtils.extractUsername(request.getHeader("Authorization").substring(7));
		User user = userService.loadUserByUsername(username);
		// sort =az | za | datewise(default)
		// filter-> completed=true/false | enddate=dd-MM-yyyy | priority=[0-10]
		List<Todo> todos= todoService.retrieveAllTodo(user);
		
		if(sort!=null) {
			if(sort.equalsIgnoreCase("az")) todos.sort(Comparator.comparing(Todo::getTitle));
			else if(sort.equalsIgnoreCase("za")) todos.sort(Comparator.comparing(Todo::getTitle,Comparator.reverseOrder()));
		}
		else todos.sort(Comparator.comparing(Todo::getEndDate));
		if(completed!=null) todos=todos.stream().filter(t->t.isCompleted()==completed).toList();
		if(priority!=null) todos=todos.stream().filter(t->t.getPriority()==priority).toList();
		if(endDate!=null) todos=todos.stream().filter(t->t.getEndDate().isEqual(endDate)).toList();
		if(todos.size()>0) return ResponseEntity.ok(todos);
		else return ResponseEntity.noContent().build();
	}

	@GetMapping("/todo/overdue")
	public ResponseEntity<?> retrieveOverdueTodo(HttpServletRequest request) {
		username = jwtUtils.extractUsername(request.getHeader("Authorization").substring(7));
		User user = userService.loadUserByUsername(username);
		List<Todo> todos= todoService.retrieveAllOverdues(user);
		if(todos.size()>0) return ResponseEntity.ok(todos);
		else return ResponseEntity.ok("No overdues. You are all caught up!");
	}

	@GetMapping("/todo/today")
	public ResponseEntity<?> retrieveTodayTodo(HttpServletRequest request) {
		username = jwtUtils.extractUsername(request.getHeader("Authorization").substring(7));
		User user = userService.loadUserByUsername(username);
		List<Todo> todos= todoService.retrieveAllTodayTodo(user);
		if(todos.size()>0) return ResponseEntity.ok(todos);
		else
			return ResponseEntity.ok("No todo for today!");
	}

}
