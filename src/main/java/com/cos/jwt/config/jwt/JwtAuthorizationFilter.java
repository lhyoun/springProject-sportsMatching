package com.cos.jwt.config.jwt;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.cos.jwt.domain.person.Person;
import com.cos.jwt.domain.person.PersonRepository;

public class JwtAuthorizationFilter implements Filter {

	private PersonRepository personRepository;

	public JwtAuthorizationFilter(PersonRepository personRepository) {
		this.personRepository = personRepository;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		System.out.println("JwtAuthorizationFilter 작동");

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		
		String jwtToken = req.getHeader(JwtProps.header);

		if (jwtToken == null) {
			PrintWriter out = resp.getWriter();
			out.print("jwtToken not found");
			System.out.println("토큰 없음");
			out.flush();
		} else {
			jwtToken = jwtToken.replace(JwtProps.auth, "");
			System.out.println("토큰 있음");
			try {
				int personId = JWT.require(Algorithm.HMAC512(JwtProps.secret)).build().verify(jwtToken).getClaim("id").asInt();
				HttpSession session = req.getSession();
				Person personEntity = personRepository.findById(personId).get();
				session.setAttribute("principal", personEntity);
				chain.doFilter(request, response);
			} catch (Exception e) {
				PrintWriter out = resp.getWriter();
				out.print("verify fail");
				out.flush();
				System.out.println("catch");
			}
		}
	}
}