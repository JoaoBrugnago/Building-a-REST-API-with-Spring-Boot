package example.cashcard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//-- A anotação @SpringBootTest configura um ambiente de teste para a aplicação Spring Boot. O parâmetro webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT indica que a aplicação será iniciada com uma porta aleatória durante o teste, evitando conflitos com outros serviços que possam estar rodando.
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class CashcardApplicationTests {
  @Autowired
	//-- Essa anotação é um "preenchimento automático" do Spring, a variável será setada assim que essa classe for reconhecida como um Bean
  TestRestTemplate restTemplate;
  //-- O TestRestTemplate é injetado automaticamente pelo Spring. Ele é uma ferramenta que facilita a realização de requisições HTTP durante testes, simulando o comportamento de um cliente.

  @Test
  //-- A anotação @Test indica que este método é um teste que será executado pelo framework de testes JUnit. 
  void shouldReturnACashCardWhenDataIsSaved() {
    ResponseEntity<String> response = restTemplate
			.withBasicAuth("sarah1", "abc123")
			.getForEntity("/cashcards/99", String.class);
    //-- O método getForEntity faz uma requisição HTTP GET para a URL "/cashcards/99" e espera uma resposta do tipo String. O objeto ResponseEntity armazena a resposta HTTP, incluindo o corpo da resposta e o código de status.
    
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    //-- Aqui, estamos verificando se o código de status da resposta é 200 OK. O assertThat é fornecido pelo AssertJ e é usado para realizar uma verificação no teste.

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		//-- O método JsonPath.parse(response.getBody()) faz o parsing (análise) do corpo da resposta HTTP, que é uma string em formato JSON. O resultado é armazenado em um objeto DocumentContext, que permite navegar e consultar dados dentro da estrutura JSON de maneira conveniente.

		Integer id = documentContext.read("$.id");
		//-- Usando o objeto documentContext, o método read("$.id") extrai o valor do campo "id" do JSON. O caminho "$.id" segue a sintaxe do JsonPath, onde "$" representa a raiz do JSON e ".id" indica o campo "id". O valor extraído é armazenado na variável id do tipo Integer.

		assertThat(id).isNotNull();
		//-- A função assertThat, do AssertJ, verifica se o valor da variável id não é nulo. Isso garante que o campo "id" existe no JSON e possui um valor válido. Se id for nulo, o teste falhará.

		assertThat(id).isEqualTo(99);
		//-- Verifica se o valor da variável id é 99

		Double amount = documentContext.read("$.amount");
		assertThat(amount).isEqualTo(123.45);
  }

	@Test
	void shouldNotReturnACashCardWithAnUnknownId() {
		ResponseEntity<String> response = restTemplate
			.withBasicAuth("sarah1", "abc123")
			.getForEntity("/cashcards/1000", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isBlank();
	}

	@Test
	@DirtiesContext
	void shouldCreateANewCashCard() {
		CashCard newCashCard = new CashCard(null, 250.00, null);
		ResponseEntity<Void> createResponse = restTemplate
			.withBasicAuth("sarah1", "abc123")
			.postForEntity("/cashcards", newCashCard, Void.class);

		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		URI locationOfNewCashCard = createResponse.getHeaders().getLocation();
		ResponseEntity<String> getResponse = restTemplate
			.withBasicAuth("sarah1", "abc123")
			.getForEntity(locationOfNewCashCard, String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Integer id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		assertThat(id).isNotNull();
		assertThat(amount).isEqualTo(250.00);
	}

	@Test
	void shouldReutrnAllCashCardsWhernListIsRequested() {
		ResponseEntity<String> response = restTemplate
			.withBasicAuth("sarah1", "abc123")
			.getForEntity("/cashcards", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());

		int cashCardCount = documentContext.read("$.length()");
		assertThat(cashCardCount).isEqualTo(3);

		JSONArray ids = documentContext.read("$..id");
		assertThat(ids).containsExactlyInAnyOrder(99, 100, 101);

		JSONArray amounts = documentContext.read("$..amount");
		assertThat(amounts).containsExactlyInAnyOrder(123.45, 1.0, 150.00);
	}

	@Test
	void shouldReturnAPageOfCashCards() {
		ResponseEntity<String> response = restTemplate
			.withBasicAuth("sarah1", "abc123")
			.getForEntity("/cashcards?page=0&size=1&sort=amount,desc", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());

		JSONArray read = documentContext.read("$[*]");
		assertThat(read.size()).isEqualTo(1);

		double amount = documentContext.read("$[0].amount");
		assertThat(amount).isEqualTo(150.00);
	}

	@Test
	void shouldReturnASortedPafeOfCashCardsWithNoParametersAndUseDefaultValues() {
		ResponseEntity<String> response = restTemplate
			.withBasicAuth("sarah1", "abc123")
			.getForEntity("/cashcards", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());

		JSONArray page = documentContext.read("$[*]");
		assertThat(page.size()).isEqualTo(3);

		JSONArray amounts = documentContext.read("$..amount");
		assertThat(amounts).containsExactly(1.00, 123.45, 150.00);
	}

	@Test
	void shouldNotReturnACashCardWhenUsingBadCredentials() {
		ResponseEntity<String> response = restTemplate
			.withBasicAuth("BAD-USER", "abc123")
			.getForEntity("/cashcards/99", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

		response = restTemplate
			.withBasicAuth("sarah1", "BAD-PASSWORD")
			.getForEntity("/cashcards/99", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void shouldRejectUsersWhoAreNotCardOwners() {
    ResponseEntity<String> response = restTemplate
      .withBasicAuth("hank-owns-no-cards", "qrs456")
      .getForEntity("/cashcards/99", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	void shouldNotAllowAccessToCashCardsTheyDoNotOwn() {
		ResponseEntity<String> response = restTemplate
			.withBasicAuth("sarah1", "abc123")
			.getForEntity("/cashcards/102", String.class); // kumar2's data
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	@DirtiesContext
	void shouldUpdateAnExistingCashCard() {
		CashCard cashCardUpdate = new CashCard(null, 19.99, null);
		HttpEntity<CashCard> request = new HttpEntity<>(cashCardUpdate);
		ResponseEntity<Void> response = restTemplate
			.withBasicAuth("sarah1", "abc123")
			.exchange("/cashcards/99", HttpMethod.PUT, request, Void.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<String> getResponse = restTemplate
			.withBasicAuth("sarah1", "abc123")
			.getForEntity("/cashcards/99", String.class);

		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		assertThat(id).isEqualTo(99);
		assertThat(amount).isEqualTo(19.99);
	}

	@Test
	void shouldNotUpdateACashCardThatDoesNotExist() {
		CashCard unknownCard = new CashCard(null, 19.99, null);
		HttpEntity<CashCard> request = new HttpEntity<>(unknownCard);
		ResponseEntity<Void> response = restTemplate
			.withBasicAuth("sarah1", "abc123")
			.exchange("/cashcards/99999", HttpMethod.PUT, request, Void.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotUpdateACashCardThatIsOwnedBySomeoneElse() {
		CashCard kumarsCard = new CashCard(null, 333.33, null);
		HttpEntity<CashCard> request = new HttpEntity<>(kumarsCard);
		ResponseEntity<Void> response = restTemplate
			.withBasicAuth("sarah1", "abc123")
			.exchange("/cashcards/102", HttpMethod.PUT, request, Void.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	@DirtiesContext
	void shouldDeleteAnExistingCashCard() {
		ResponseEntity<Void> response = restTemplate
			.withBasicAuth("sarah1", "abc123")
			.exchange("/cashcards/99", HttpMethod.DELETE, null, Void.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);


		ResponseEntity<String> getResponse = restTemplate
      .withBasicAuth("sarah1", "abc123")
      .getForEntity("/cashcards/99", String.class);

    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotDeleteACashCardThatDoesNotExist() {
		ResponseEntity<Void> deleteResponse = restTemplate
			.withBasicAuth("sarah1", "abc123")
			.exchange("/cashcards/99999", HttpMethod.DELETE, null, Void.class);

		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotAllowDeletionOfCashCardTheyDoNotOwn() {
		ResponseEntity<Void> deleteResponse = restTemplate
			.withBasicAuth("sarah1", "abc123")
			.exchange("/cashcards/102", HttpMethod.DELETE, null, Void.class);

		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);


		ResponseEntity<String> getResponse = restTemplate
      .withBasicAuth("kumar2", "xyz789")
      .getForEntity("/cashcards/102", String.class);

    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
}

/*
Função da anotação @Test
@Test é usada para marcar um método como um teste que será executado pelo JUnit. Ela sozinha não configura um contexto Spring nem inicializa os componentes da sua aplicação. Basicamente, o JUnit executará o método como um teste unitário puro, sem se preocupar com dependências ou o contexto Spring.

Função da anotação @SpringBootTest
@SpringBootTest, por outro lado, inicializa todo o contexto da aplicação Spring. Isso significa que o Spring Boot carregará todos os componentes necessários (beans, configurações, etc.) como se a aplicação estivesse realmente sendo executada, permitindo que você faça testes de integração completos.

Se você remover o @SpringBootTest, o Spring não será inicializado, e qualquer injeção de dependência (como @Autowired) ou funcionalidades específicas do Spring, como requisições via TestRestTemplate, não funcionarão.
*/