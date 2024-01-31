package com.ruslanio.extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class ExtensionApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExtensionApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}

@RestController
class OpenAiProxyController {

	private static final Logger logger = LoggerFactory.getLogger(OpenAiProxyController.class);

	@Autowired
	private RestTemplate restTemplate;

	@Value("${openai.api.key}")
	private String openAiApiKey;

	@Value("${openai.api.host}")
	private String openAiHost;

	public OpenAiProxyController(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@PostMapping("/checkByUrl")
	public String checkByUrl(@RequestBody String url) {
		logger.info("Received checkByUrl request for URL: {}", url);
		String apiUrl = openAiHost + "/chat/completions";
		return makeOpenAiRequest(apiUrl, getCheckPromt(url));
	}

	@PostMapping("/isArticle")
	public String isArticle(@RequestBody String url) {
		logger.info("Received isArticle request for URL: {}", url);
		String apiUrl = openAiHost + "/chat/completions";
		return makeOpenAiRequest(apiUrl, getIsArticlePromt(url));
	}

	private String makeOpenAiRequest(String apiUrl, String content) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Bearer " + openAiApiKey);

		String requestBody = String.format("{\"messages\":[{\"role\":\"system\",\"content\":\"%s\"}],\"model\":\"gpt-3.5-turbo\"}", content);

		HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

		return restTemplate.postForObject(apiUrl, requestEntity, String.class);
	}

	private String getCheckPromt(String url) {
		return String.format("%s Please analyze the main thesis or argument of the article and compare it with relevant statistical data and information you have access to. I am interested in whether the claims made in the article are supported or contradicted by the data. Try to give an answer in the range of 100 to 150 words", url);
	}

	private String getIsArticlePromt(String url) {
		return String.format("can you classify information on this page %s as an article? Reply with one word either \"yes\" or \"no\"", url);
	}

}

