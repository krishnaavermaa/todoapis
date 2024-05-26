package com.reskilling.todoapis.utils
;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtility {
	private final String SECRET="itsNotAnySecretRereskillingProject";
	private List<String> expiredTokens;
	
	public JwtUtility() {
		expiredTokens=new ArrayList<String>();
	}
		
	public String generateToken(UserDetails userDetails) {
		Map<String, Object> claims=new HashMap<>();
		return createToken(claims,userDetails.getUsername());
	}
	
	private String createToken(Map<String, Object> claims, String subject) {
		return Jwts.builder().setClaims(claims)
				.setSubject(subject)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis()+1000*60*60*10))
				.signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).compact();
	}
	
	public boolean validateToken(String token, UserDetails userDetails) {
		final String username=extractUsername(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}
	
	
	private boolean isTokenExpired(String token) {
		System.out.println("EXPIRED TOKENS="+expiredTokens.size());
		return extractExpiration(token).isBefore(LocalDate.now()) || expiredTokens.contains(token);
	}
	
	private LocalDate extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}
	
	public String extractUsername(String token) {
		return extractClaim(token,Claims::getSubject);
	}
	
	public void forceExpireToken(String token) {
		expiredTokens=expiredTokens.stream()
				.filter(t->!extractExpiration(t).isBefore(LocalDate.now()))
				.collect(Collectors.toList());
		System.out.println(expiredTokens.getClass());
		expiredTokens.add(token);
		System.out.println("EXPIRED TOKENS="+expiredTokens.size());
	}
	
	private<T> T extractClaim(String token, Function<Claims,T> claimsResolver) {
		final Claims claims=extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	private Claims extractAllClaims(String token) {
		// TODO Auto-generated method stub
//		Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
		return Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).build().parseClaimsJws(token).getBody();

	}
	

}
