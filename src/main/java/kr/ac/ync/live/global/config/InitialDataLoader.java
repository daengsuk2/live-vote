package kr.ac.ync.live.global.config;

import kr.ac.ync.live.user.Role;
import kr.ac.ync.live.user.User;
import kr.ac.ync.live.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Creates the first instructor account (values come from .env) when it does not exist yet. */
@Component
public class InitialDataLoader implements ApplicationRunner {

	private final UserRepository users;
	private final PasswordEncoder encoder;
	private final String email;
	private final String password;
	private final String name;

	public InitialDataLoader(UserRepository users, PasswordEncoder encoder,
			@Value("${app.seed.instructor-email}") String email,
			@Value("${app.seed.instructor-password}") String password,
			@Value("${app.seed.instructor-name}") String name) {
		this.users = users;
		this.encoder = encoder;
		this.email = email;
		this.password = password;
		this.name = name;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (users.findByEmail(email).isEmpty()) {
			users.save(new User(email, encoder.encode(password), name, Role.INSTRUCTOR));
		}
	}
}
