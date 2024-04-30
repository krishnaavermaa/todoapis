package com.reskilling.todoapis.controller;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.reskilling.todoapis.model.User;
import com.reskilling.todoapis.service.UserService;
import com.reskilling.todoapis.utils.JwtUtility;

import jakarta.servlet.http.HttpServletRequest;

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
			System.out.println("uptokn=" + uptoken);
			Authentication auth = authenticationmanager.authenticate(uptoken);
			System.out.println("auth=" + auth);
			System.out.println("controller security context=" + SecurityContextHolder.getContext() + "& auth=" + auth);
			SecurityContextHolder.getContext().setAuthentication(auth);
			System.out.println(SecurityContextHolder.getContext().getAuthentication());
			UserDetails userDetails = userService.loadUserByUsername(user.getUsername());
			System.out.println("even here");
			String jwt = jwtUtils.generateToken(userDetails);
			System.out.println("jwt= " + jwt);
			return ResponseEntity.ok(jwt);
		} catch (BadCredentialsException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body("Invalid Username or Password. Kindly signup if you don't have an account.");
		}
	}

	@PostMapping("/user/signup")
	public ResponseEntity<?> registerUser(@RequestBody User user) {
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
				return ResponseEntity.ok("Logged out successfully!");
			}
		}
		return ResponseEntity.badRequest().body("Invalid token or no token provided.");
	}

}
