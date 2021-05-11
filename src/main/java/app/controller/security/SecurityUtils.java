package app.controller.security;

import app.jpa_repo.PersonRepository;
import app.model.users.Person;
import com.vaadin.flow.server.ServletHelper.RequestType;
import com.vaadin.flow.shared.ApplicationConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * SecurityUtils takes care of all such static operations that have to do with
 * security and querying rights from different beans of the UI.
 */
public final class SecurityUtils{

	public static ArrayList<String> online =new ArrayList<>();

	private SecurityUtils() {
		// Util methods only
	}

	/**
	 * Tests if the request is an internal framework request. The test consists of
	 * checking if the request parameter is present and if its value is consistent
	 * with any of the request types know.
	 *
	 * @param request {@link HttpServletRequest}
	 * @return true if is an internal framework request. False otherwise.
	 */
	static boolean isFrameworkInternalRequest(HttpServletRequest request) {
		final String parameterValue = request.getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER);
		return parameterValue != null
				&& Stream.of(RequestType.values()).anyMatch(r -> r.getIdentifier().equals(parameterValue));
	}

	/**
	 * Tests if some user is authenticated. As Spring Security always will create an {@link AnonymousAuthenticationToken}
	 * we have to ignore those tokens explicitly.
	 */
	static boolean isUserLoggedIn() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication != null
				&& !(authentication instanceof AnonymousAuthenticationToken)
				&& authentication.isAuthenticated();
	}

	public static boolean isUserAdmin() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		SimpleGrantedAuthority adminRole = new SimpleGrantedAuthority(Person.Role.ADMIN.toString());
		return authentication != null
				&& !(authentication instanceof AnonymousAuthenticationToken)
				&& authentication.isAuthenticated()
				&& authentication.getAuthorities().contains(adminRole);
	}

	public static boolean isUserStudent() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		SimpleGrantedAuthority studentRole = new SimpleGrantedAuthority(Person.Role.STUDENT.toString());
		return authentication != null
				&& !(authentication instanceof AnonymousAuthenticationToken)
				&& authentication.isAuthenticated()
				&& authentication.getAuthorities().contains(studentRole);
	}

	public static boolean isUserTeacher() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		SimpleGrantedAuthority teacherRole = new SimpleGrantedAuthority(Person.Role.TEACHER.toString());
		return authentication != null
				&& !(authentication instanceof AnonymousAuthenticationToken)
				&& authentication.isAuthenticated()
				&& authentication.getAuthorities().contains(teacherRole);
	}



	/**
	 * Returns the current user
	 * @param personRepository the JPA repo to search the user in
	 * @return	the current user
	 */
	public static Person getCurrentUser(PersonRepository personRepository) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return personRepository.findByUsername(authentication.getName());
	}
}
