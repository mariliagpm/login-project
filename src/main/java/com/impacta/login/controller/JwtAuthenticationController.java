package com.impacta.login.controller;

import com.impacta.login.config.CustomTokenEnhancer;
import com.impacta.login.config.JwtTokenUtil;
import com.impacta.login.model.JwtRequest;
import com.impacta.login.model.LoginDao;
import com.impacta.login.model.LoginDto;
import com.impacta.login.model.UserLogged;
import com.impacta.login.service.JwtUserDetailsService;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;

import org.apache.http.NameValuePair;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class JwtAuthenticationController extends AbstractController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private JwtUserDetailsService userDetailsService;

	private static final Logger LOGGER = Logger.getLogger(JwtAuthenticationController.class);

	protected static final String FEEDBACK_MESSAGE_KEY_LOGIN_CREATED = "feedback.message.login.created";
	protected static final String ERROR_MESSAGE_KEY_LOGIN_WAS_NOT_CREATED = "error.message.login.was.not.created";
	protected static final String ERROR_MESSAGE_KEY_EMAIL_EMPTY = "error.message.email.not.blank";
	protected static final String ERROR_MESSAGE_KEY_USERNAME_EMPTY = "error.message.username.not.blank";
	protected static final String ERROR_MESSAGE_KEY_PASSWORD_EMPTY = "error.message.password.not.blank";
	protected static final String ERROR_MESSAGE_KEY_USER_ALREADY_HAS_ACCOUNT = "error.message.user.has.account";
	protected static final String ERROR_MESSAGE_EMAIL_ALREADY_USED = "error.message.email.already.used";

	// LoginMessages
	protected static final String ERROR_MESSAGE_LOGIN_BAD_CREDENTIALS = "error.message.login.bad.credencials";
	protected static final String ERROR_MESSAGE_LOGIN_USER_NOT_FOUND = "error.message.login.user.not.found";
	protected static final String FEEDBACK_MESSAGE_LOGIN_SUCCESS = "error.message.login.sucess";
	protected static final String ERROR_MESSAGE_LOGIN_DEFAULT = "error.message.login.default";

	/**
	 * @param authenticationRequest
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "api/login/authenticate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {
		try {
			LOGGER.info("Login process was started");
			userDetailsService.userNameAlreadExist(authenticationRequest.getUsername());
			authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
			final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
			final String token = jwtTokenUtil.generateToken(userDetails);

			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost httpPost = new HttpPost("http://localhost:9000/oauth/token");
			httpPost.addHeader(

					BasicScheme.authenticate(new UsernamePasswordCredentials("clientId", "secret"), "UTF-8", true));
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("grant_type", "passwor"));
			params.add(new BasicNameValuePair("username", "user"));
			params.add(new BasicNameValuePair("password", "pass"));

			UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, "UTF-8");
			httpPost.setEntity(ent);
			org.apache.http.HttpEntity httpResponse =  httpClient.execute(httpPost).getEntity();
			System.out.println(httpResponse.getContent().read());

			LOGGER.info("Token was generated as " + token);
			UserLogged userLogged = new UserLogged();
			userLogged.setToken(token);
			userLogged.setUsername(userDetails.getUsername());
			LOGGER.info("User is logged");

			return new ResponseEntity<>(addFeedbackMessage(FEEDBACK_MESSAGE_LOGIN_SUCCESS, userLogged),
					HttpStatus.CREATED);

		} catch (UsernameNotFoundException useNotFound) {
			LOGGER.info("Username was not found");
			return new ResponseEntity<>(
					addErrorMessage(ERROR_MESSAGE_LOGIN_USER_NOT_FOUND, authenticationRequest.getUsername()),
					HttpStatus.UNAUTHORIZED);

		} catch (BadCredentialsException bad) {
			LOGGER.info("Credentials were wrong");
			return new ResponseEntity<>(
					addErrorMessage(ERROR_MESSAGE_LOGIN_BAD_CREDENTIALS, authenticationRequest.getUsername()),
					HttpStatus.UNAUTHORIZED);

		} catch (Exception e) {
			LOGGER.info("Login failed " + e);
			return new ResponseEntity<>(
					addErrorMessage(ERROR_MESSAGE_LOGIN_DEFAULT, authenticationRequest.getUsername()),
					HttpStatus.UNAUTHORIZED);
		}
	}

	@RequestMapping(value = "api/login/register", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> saveUser(@Valid @RequestBody LoginDto user) throws Exception {
		try {
			if (user.getUsername() == null || user.getUsername().isEmpty()) {
				return new ResponseEntity<>(addErrorMessage(ERROR_MESSAGE_KEY_USERNAME_EMPTY, user.getUsername()),
						HttpStatus.INTERNAL_SERVER_ERROR);

			}
			if (user.getEmail() == null || user.getEmail().isEmpty()) {
				return new ResponseEntity<>(addErrorMessage(ERROR_MESSAGE_KEY_EMAIL_EMPTY, user.getUsername()),
						HttpStatus.INTERNAL_SERVER_ERROR);

			}
			if (user.getPassword() == null || user.getPassword().isEmpty()) {
				return new ResponseEntity<>(addErrorMessage(ERROR_MESSAGE_KEY_PASSWORD_EMPTY, user.getUsername()),
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
			List<LoginDao> listaEmail = userDetailsService.buscaPorEmail(user);
			if (listaEmail.size() > 0) {

				return new ResponseEntity<>(addErrorMessage(ERROR_MESSAGE_EMAIL_ALREADY_USED, user.getUsername()),
						HttpStatus.INTERNAL_SERVER_ERROR);

			}
			if (userDetailsService.userNameAlreadExist(user.getUsername())) {

				ResponseEntity<?> repEntity = new ResponseEntity<>(
						addErrorMessage(ERROR_MESSAGE_KEY_USER_ALREADY_HAS_ACCOUNT, user.getUsername()),
						HttpStatus.INTERNAL_SERVER_ERROR);

				return repEntity;

			}
			userDetailsService.save(user);
			LOGGER.info("User is created");

			return new ResponseEntity<>(addFeedbackMessage(FEEDBACK_MESSAGE_KEY_LOGIN_CREATED, user.getUsername()),
					HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(addErrorMessage(ERROR_MESSAGE_KEY_LOGIN_WAS_NOT_CREATED, user.getUsername()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private void authenticate(String username, String password) throws Exception {
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
		} catch (DisabledException e) {
			System.out.println("User disable");
			throw new Exception("USER_DISABLED", e);
		} catch (BadCredentialsException e) {
			System.out.println("Invalid_Credential");
			throw new BadCredentialsException("INVALID_CREDENTIALS", e);
		}
	}
}
