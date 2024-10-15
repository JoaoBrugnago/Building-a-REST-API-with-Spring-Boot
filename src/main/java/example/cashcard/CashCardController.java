package example.cashcard;

import java.net.URI;
import java.util.List;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity; // Importa a classe ResponseEntity para construir a resposta HTTP.
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping; // Importa a anotação para mapear requisições GET.
import org.springframework.web.bind.annotation.PathVariable; // Importa a anotação para extrair variáveis da URL.
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping; // Importa a anotação para mapear URLs de requisição.
import org.springframework.web.bind.annotation.RestController; // Importa a anotação para definir um controlador REST.
import org.springframework.web.util.UriComponentsBuilder;

@RestController
//-- A anotação @RestController indica que esta classe é um controlador REST, que manipula requisições HTTP e retorna respostas diretamente no corpo da resposta.
@RequestMapping("/cashcards")
//-- A anotação @RequestMapping define a URL base para todas as requisições que serão tratadas por este controlador. Neste caso, todas as requisições para "/cashcards" serão tratadas aqui.
public class CashCardController {
  private final CashCardRepository cashCardRepository; // Declaração da variável para o repositório de CashCard.

  // Construtor que injeta o CashCardRepository na classe CashCardController.
  private CashCardController(CashCardRepository cashCardRepository) {
      this.cashCardRepository = cashCardRepository; // Atribui o repositório recebido à variável de instância.
  }

  @GetMapping("/{requestedId}")
  //-- A anotação @GetMapping mapeia requisições HTTP GET para o método findById. O caminho "/{requestedId}" significa que o método responderá a requisições para URLs como "/cashcards/99", onde "99" é o valor de requestedId.
  private ResponseEntity<CashCard> findById(@PathVariable Long requestedId, Principal principal) {
    //-- O parâmetro requestedId é extraído da URL usando a anotação @PathVariable. Isso permite que o valor do ID da requisição seja passado como argumento para o método.
    
    CashCard cashCard = findCashCard(requestedId, principal);

    if (cashCard != null) {
        //-- Verifica se o CashCard foi encontrado no repositório.
        return ResponseEntity.ok(cashCard);
        //-- Se encontrado, retorna uma resposta HTTP 200 OK com o objeto CashCard no corpo da resposta.
    } else {
        return ResponseEntity.notFound().build();
        //-- Se não encontrado, retorna uma resposta HTTP 404 Not Found, indicando que o recurso não foi encontrado.
    }
  }

  @PostMapping
  private ResponseEntity<Void> createCashCard(@RequestBody CashCard newCashCardRequest, UriComponentsBuilder ucb, Principal principal) {
    CashCard cashCardWithOwner = new CashCard(null, newCashCardRequest.amount(), principal.getName());
    CashCard savedCashCard = cashCardRepository.save(cashCardWithOwner);
    URI locationOfNewCashCard = ucb
      .path("cashcards/{id}")
      .buildAndExpand(savedCashCard.id())
      .toUri();
    return ResponseEntity.created(locationOfNewCashCard).build();
  }

  @GetMapping
  private ResponseEntity<List<CashCard>> findAll(Pageable pageable, Principal principal) {
    Page<CashCard> page = cashCardRepository.findByOwner(
      principal.getName(),
      PageRequest.of(
        pageable.getPageNumber(),
        pageable.getPageSize(),
        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
      )
    );

    return ResponseEntity.ok(page.getContent());
  }

  @PutMapping("/{requestedId}")
  private ResponseEntity<Void> putCashCard(@PathVariable Long requestedId, @RequestBody CashCard cashCardUpdate, Principal principal) {
    CashCard cashCard = findCashCard(requestedId, principal);
    if (cashCard != null) {
      CashCard updatedCashCard = new CashCard(cashCard.id(), cashCardUpdate.amount(), principal.getName());
      cashCardRepository.save(updatedCashCard);
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.notFound().build();
  }

  @DeleteMapping("/{id}")
  private ResponseEntity<Void> deleteCashCard(@PathVariable Long id, Principal principal) {
    if (cashCardRepository.existsByIdAndOwner(id, principal.getName())) {
      cashCardRepository.deleteById(id);
      return ResponseEntity.noContent().build();
    }

    return ResponseEntity.notFound().build();
  }

  private CashCard findCashCard(Long requestedId, Principal principal) {
    return cashCardRepository.findByIdAndOwner(requestedId, principal.getName());
  }
}
