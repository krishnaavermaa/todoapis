package com.reskilling.todoapis.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.reskilling.todoapis.filters.JwtAuthenticationFilter;
import com.reskilling.todoapis.service.UserService;

@Configuration
@EnableWebSecurity
@ComponentScan("com.reskilling.todoapis")
public class SecurityConfig {
	
	@Autowired
	private UserService userService;
	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;
	
	 @Bean
	    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		 	
		 	http.csrf(c->c.disable())
		 		.authorizeHttpRequests(req->req.requestMatchers("/login","/signup").permitAll()
		 		.anyRequest().authenticated())
		 		.sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		 	
		 	http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
	        
	        return http.build();
	    }
	 
	 @Bean
	    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
	        AuthenticationManagerBuilder authenticationManagerBuilder = 
	            http.getSharedObject(AuthenticationManagerBuilder.class);
	        authenticationManagerBuilder.userDetailsService(userService);
	        return authenticationManagerBuilder.build();
	    }
	 
}
