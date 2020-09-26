package org.sid.sec;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter{
	
	@Autowired
	private DataSource dataSource;
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		/*auth.inMemoryAuthentication()
		.withUser("admin").password("1234").roles("ADMIN","USER");
		auth.inMemoryAuthentication()
		.withUser("user").password("1234").roles("USER");*/
		
		auth.jdbcAuthentication()
		.dataSource(dataSource)
		.usersByUsernameQuery("Select username as principal, password as credentials, active from Users where username=? ")
		.authoritiesByUsernameQuery("Select username as principal, role as role from users_roles where username=? ")
		.rolePrefix("ROLE_")
		.passwordEncoder(getBCPE());
		
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.formLogin().loginPage("/login");
		http.authorizeRequests().antMatchers("/operations","/consulterCompte").hasRole("USER");  
		http.authorizeRequests().antMatchers("/saveOperation").hasRole("ADMIN");
	}
	
	@Bean
	PasswordEncoder getBCPE() {
		return new BCryptPasswordEncoder();
	}
	
}
