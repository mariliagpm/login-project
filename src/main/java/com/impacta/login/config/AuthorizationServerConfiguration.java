package com.impacta.login.config;

 
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import com.impacta.login.service.JwtUserDetailsService;

import javax.sql.DataSource;
import java.util.Arrays;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

	 @Autowired
	    private AuthenticationManager authenticationManager;

	    @Override
	    public void configure(AuthorizationServerEndpointsConfigurer endpoints)
	            throws Exception {
	        endpoints.authenticationManager(authenticationManager);
	    }

	    @Override
	    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
	        clients.inMemory().withClient("my-trusted-client")
	                .authorizedGrantTypes("password",
	                        "refresh_token", "implicit", "client_credentials", "authorization_code")
	                .authorities("CLIENT")
	                .scopes("read", "write", "trust")
	                .accessTokenValiditySeconds(3600)
	                 .resourceIds("resource")
	                .secret("mysecret");
	    }

	    @Override
	    public void configure(AuthorizationServerSecurityConfigurer oauthServer)
	            throws Exception {
	        oauthServer
	                .tokenKeyAccess("permitAll()")
	                .checkTokenAccess("permitAll()");
	    }
}
