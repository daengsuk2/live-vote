package kr.ac.ync.live.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	/** Student (participant) API: no login, so the browser has no CSRF token to send. */
	private static final String[] PARTICIPANT_API = { "/api/sessions/**", "/api/questions/**", "/api/polls/**" };

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			// CSRF stays on everywhere except the participant API. Never csrf.disable() as a whole.
			.csrf(csrf -> csrf.ignoringRequestMatchers(PARTICIPANT_API))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/css/**", "/js/**", "/vendor/**", "/img/**").permitAll()
				.requestMatchers("/", "/index.html", "/login", "/join", "/s/**", "/error/**").permitAll()
				.requestMatchers("/ws/**").permitAll()
				.requestMatchers(PARTICIPANT_API).permitAll()
				.requestMatchers("/instructor/**", "/api/instructor/**").hasRole("INSTRUCTOR")
				.anyRequest().authenticated())
			.formLogin(form -> form
				.loginPage("/login")
				.usernameParameter("email")
				.defaultSuccessUrl("/instructor", true)
				.permitAll())
			.logout(logout -> logout
				.logoutSuccessUrl("/login?logout")
				.permitAll());
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
