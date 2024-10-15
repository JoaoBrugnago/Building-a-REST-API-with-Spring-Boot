package example.cashcard;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

// A anotação @JsonTest indica que esta classe é uma classe de teste especializada
// para testes de serialização e desserialização JSON com o Spring Boot.
// Essa anotação configura o contexto do Spring apenas com as partes necessárias para
// realizar testes relacionados ao JSON.
@JsonTest
class CashCardJsonTest {

  //-- O JacksonTester é uma classe que ajuda a testar a serialização e desserialização de objetos JSON com a biblioteca Jackson. O Spring irá injetar automaticamente uma instância de JacksonTester configurada para a classe CashCard, permitindo testes de forma mais fácil.
  @Autowired
  private JacksonTester<CashCard> json;

  @Autowired
  private JacksonTester<CashCard[]> jsonList;

  private CashCard[] cashCards;

  @BeforeEach
  void setUp() {
    cashCards = Arrays.array(
      new CashCard(99L, 123.45, "sarah1"),
      new CashCard(100L, 1.00, "sarah1"),
      new CashCard(101L, 150.00, "sarah1")
    );
  }

  //-- O método abaixo é um teste unitário, marcado com a anotação @Test do JUnit. Ele testa a serialização do objeto CashCard para JSON.
  @Test
  void cashCardSerializationTest() throws IOException {
    //-- Cria uma instância do CashCard com id 99L e amount 123.45.
    CashCard cashCard = cashCards[0];
    
    //-- Compara o JSON resultante da serialização do cashCard com o JSON esperado que está contido no arquivo "expected.json".
    assertThat(json.write(cashCard)).isStrictlyEqualToJson("single.json");
    
    //-- Verifica se o JSON gerado possui um caminho JSON que corresponde ao valor do campo "id".
    assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.id");
    
    //-- Extrai o valor do campo "id" do JSON e verifica se ele é igual a 99.
    assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.id").isEqualTo(99);
    
    //-- Verifica se o JSON gerado possui um caminho JSON que corresponde ao valor do campo "amount".
    assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.amount");
    
    //-- Extrai o valor do campo "amount" do JSON e verifica se ele é igual a 123.45.
    assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.amount").isEqualTo(123.45);
  }

  @Test
  //-- O método abaixo é um teste unitário, marcado com a anotação @Test do JUnit. Ele testa a deserialização de um JSON em um objeto CashCard.
  void cashCardDeserializationTest() throws IOException {
    //-- Define a string que representa um JSON esperado, contendo os campos "id" e "amount".
    String expected = """
            {
                "id":99,
                "amount":123.45,
                "owner": "sarah1"
            }
            """;
    
    //-- Compara o objeto CashCard resultante da deserialização do JSON com uma nova instância do CashCard, garantindo que os valores correspondam.
    assertThat(json.parse(expected)).isEqualTo(new CashCard(99L, 123.45, "sarah1"));
    
    //-- Extrai o valor do campo "id" do objeto deserializado e verifica se ele é igual a 99.
    assertThat(json.parseObject(expected).id()).isEqualTo(99);
    
    //-- Extrai o valor do campo "amount" do objeto deserializado e verifica se ele é igual a 123.45.
    assertThat(json.parseObject(expected).amount()).isEqualTo(123.45);
  }

  @Test
  void cashCardListSerializationTest() throws IOException {
    assertThat(jsonList.write(cashCards)).isStrictlyEqualToJson("list.json");
  }

  @Test
  void cashCardListDeserializationTest() throws IOException {
    String expected = 
      """
        [
            { "id": 99, "amount": 123.45, "owner": "sarah1" },
            { "id": 100, "amount": 1.00, "owner": "sarah1" },
            { "id": 101, "amount": 150.00, "owner": "sarah1" }
         ]
      """;

    assertThat(jsonList.parse(expected)).isEqualTo(cashCards);
  }
}
