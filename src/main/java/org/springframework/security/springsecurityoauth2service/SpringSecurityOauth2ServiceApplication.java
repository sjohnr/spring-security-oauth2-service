package org.springframework.security.springsecurityoauth2service;

import java.util.Objects;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

@SpringBootApplication
@EnableConfigurationProperties(OAuth2ClientProperties.class)
public class SpringSecurityOauth2ServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringSecurityOauth2ServiceApplication.class, args);
	}

	@Bean
	public ClientRegistrationRepository clientRegistrationRepository(OAuth2ClientProperties properties) {
		var clientRegistrations = new OAuth2ClientPropertiesMapper(properties)
			.asClientRegistrations().values().stream().toList();
		return new InMemoryClientRegistrationRepository(clientRegistrations);
	}

	@Bean
	public OAuth2AuthorizedClientService authorizedClientService(ClientRegistrationRepository clientRegistrationRepository) {
		return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
	}

	@Bean
	public OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistrationRepository, OAuth2AuthorizedClientService authorizedClientService) {
		var authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
			.clientCredentials()
			.build();
		var authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);
		authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

		return authorizedClientManager;
	}

	@Bean
	public CommandLineRunner commandLineRunner(OAuth2AuthorizedClientManager authorizedClientManager) {
		return (args) -> {
			var authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId("client-1")
				.principal("user-service")
				.build();
			var authorizedClient = Objects.requireNonNull(authorizedClientManager.authorize(authorizeRequest));
			System.out.println(authorizedClient.getAccessToken().getTokenValue());
		};
	}

}
