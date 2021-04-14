package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class DiscoolApplication {

	public static void main(String[] args) {
		BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
		System.out.println("password : " + bc.encode("password"));
		System.out.println("t1 : " + bc.encode("t1"));
		System.out.println("t2 : " + bc.encode("t2"));
		System.out.println("t3 : " + bc.encode("t3"));
		System.out.println("s1 : " + bc.encode("s1"));
		System.out.println("s2 : " + bc.encode("s2"));
		System.out.println("s3 : " + bc.encode("s3"));




		SpringApplication.run(DiscoolApplication.class, args); }
}
