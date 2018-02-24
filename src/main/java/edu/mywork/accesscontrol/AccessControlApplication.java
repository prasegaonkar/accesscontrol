package edu.mywork.accesscontrol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Stream;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@SpringBootApplication
public class AccessControlApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccessControlApplication.class, args);
	}
}

@Component
class Initializer implements CommandLineRunner {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private PrivilegeRepository privilegeRepository;

	@Override
	public void run(String... arg0) throws Exception {
		Stream.of("read", "write").forEach(p -> privilegeRepository.save(new Privilege(p)));

		Privilege read = privilegeRepository.findByName("read");
		Privilege write = privilegeRepository.findByName("write");

		roleRepository.save(new Role("admin", Arrays.asList(read, write)));
		roleRepository.save(new Role("user", Arrays.asList(read)));

		Stream.of("alex", "bill", "cathy", "dave", "emily", "frienze", "george", "harry", "ivan", "jack", "kevin",
				"lewis", "mike", "nancy", "oliver", "peter", "quincy", "roger", "steve", "tim", "ursula", "victor",
				"warren", "xavier", "zach").forEach(name -> {
					if (name.contains("a")) {
						userRepository.save(new User(name, Arrays.asList(roleRepository.findByName("admin"))));
					} else {
						userRepository.save(new User(name, Arrays.asList(roleRepository.findByName("user"))));
					}
				});
		userRepository.findAll().forEach(System.out::println);

	}

}

@Entity
@Getter
@Setter
@NoArgsConstructor
class User {
	@Id
	private String id;
	@Column
	private String name;

	@ManyToMany
	@JoinTable(name = "users_roles", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
	private Collection<Role> roles = new ArrayList<>();

	public User(String name, Collection<Role> roles) {
		this.id = UUID.randomUUID().toString();
		this.name = name;
		this.roles = roles;
	}
}

@Entity
@Getter
@Setter
@NoArgsConstructor
class Role {
	@Id
	private String id;

	@Column(unique = true, nullable = false)
	private String name;

	@ManyToMany(mappedBy = "roles")
	private Collection<User> users = new ArrayList<>();

	@ManyToMany
	@JoinTable(name = "roles_privileges", joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "privilege_id", referencedColumnName = "id"))
	private Collection<Privilege> privileges = new ArrayList<>();

	public Role(String name, Collection<Privilege> privileges) {
		this.id = UUID.randomUUID().toString();
		this.name = name;
		this.privileges = privileges;
	}
}

@Entity
@Getter
@Setter
@NoArgsConstructor
class Privilege {

	@Id
	private String id;

	@Column(unique = true, nullable = false)
	private String name;

	@ManyToMany(mappedBy = "privileges")
	private Collection<Role> roles = new ArrayList<>();

	public Privilege(String name) {
		this.id = UUID.randomUUID().toString();
		this.name = name;
	}
}

@RepositoryRestResource
interface UserRepository extends JpaRepository<User, String> {

}

@RepositoryRestResource
interface RoleRepository extends JpaRepository<Role, String> {
	Role findByName(String nm);
}

@RepositoryRestResource
interface PrivilegeRepository extends JpaRepository<Privilege, String> {
	Privilege findByName(String nm);
}
