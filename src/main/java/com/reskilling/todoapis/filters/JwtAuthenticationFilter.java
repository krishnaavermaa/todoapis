package com.reskilling.todoapis.filters;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.reskilling.todoapis.model.User;
import com.reskilling.todoapis.service.UserService;
import com.reskilling.todoapis.utils.JwtUtility;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter{
	
	@Autowired
	UserService userService;
	@Autowired
	JwtUtility jwtUtils;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("INSIDE jwtFilter");
		final String authorizationHeader=request.getHeader("Authorization");
		String username=null,jwt=null;
		if(authorizationHeader!=null && authorizationHeader.startsWith("Bearer ")) {
			jwt=authorizationHeader.substring(7);
			username=jwtUtils.extractUsername(jwt);
			System.out.println("username extracted="+username);
		}
		
		if(username!=null && SecurityContextHolder.getContext().getAuthentication()==null) {
			System.out.println("authentication is null and username is extracted");
			User user=this.userService.loadUserByUsername(username);
			System.out.println("userdetails/user retrieved from database using the username= "+user.toString());
			if(jwtUtils.validateToken(jwt,user)) {
				UsernamePasswordAuthenticationToken token=new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
				System.out.println("token is valid & upauthtoken= "+token);
				token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				System.out.println("filter security context="+SecurityContextHolder.getContext()+"& auth=token="+token);
				SecurityContextHolder.getContext().setAuthentication(token);
				System.out.println("here line 53 ");
			}
		}
		System.out.println("almost OUTSIDE jwtFilter");
		filterChain.doFilter(request, response);
		
	}

}
