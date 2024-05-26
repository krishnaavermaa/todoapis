package com.reskilling.todoapis.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.reskilling.todoapis.model.User;
import com.reskilling.todoapis.service.UserService;
import com.reskilling.todoapis.utils.JwtUtility;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
public class AuthController {
	@Autowired
	private AuthenticationManager authenticationmanager;
	@Autowired
	private JwtUtility jwtUtils;

	@Autowired
	private UserService userService;

	@PostMapping("/user/login")
	public ResponseEntity<?> authenticateUser(@RequestBody User user) {
		try {
			UsernamePasswordAuthenticationToken uptoken = new UsernamePasswordAuthenticationToken(user.getUsername(),
					user.getPassword());
			Authentication auth = authenticationmanager.authenticate(uptoken);
			SecurityContextHolder.getContext().setAuthentication(auth);
			UserDetails userDetails = userService.loadUserByUsername(user.getUsername());
			String jwt = jwtUtils.generateToken(userDetails);
			return ResponseEntity.ok(jwt);
		} catch (BadCredentialsException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body("Invalid Username or Password. Kindly signup if you don't have an account.");
		}
	}

	@PostMapping("/user/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody User user, BindingResult bindingResult) {
		if(bindingResult.hasErrors()) {
			Map<String,String> errors=new HashMap<>();
			bindingResult.getFieldErrors()
			.forEach(err->
					errors.put(err.getField(), err.getDefaultMessage()));
			return ResponseEntity.badRequest().body(errors);
		}
		if (userService.loadUserByUsername(user.getUsername()) != null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username is already taken");
		}
		if (userService.saveUser(user))
			return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to create user in database");
	}

	@PostMapping("/user/logout")
	public ResponseEntity<?> logoutUser(HttpServletRequest request) {
		System.out.println("inside logging out");
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String jwtToken = authHeader.substring(7);
			String username = jwtUtils.extractUsername(jwtToken);
			UserDetails userDetails = this.userService.loadUserByUsername(username);
			if (jwtUtils.validateToken(jwtToken, userDetails)) {
				SecurityContextHolder.clearContext();
				jwtUtils.forceExpireToken(jwtToken);
				return ResponseEntity.ok("Logged out successfully!");
			}
		}
		return ResponseEntity.badRequest().body("Invalid or expired token.");
	}

}
