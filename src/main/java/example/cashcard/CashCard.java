package example.cashcard;

import org.springframework.data.annotation.Id;

record CashCard(@Id Long id, Double amount, String owner) {}

/*
O que é uma record?
A partir do Java 14, o Java introduziu um novo tipo de classe chamado record. As records são uma maneira concisa de criar classes que são usadas principalmente para armazenar dados. Elas têm algumas características e benefícios específicos:

Imutabilidade:
As records são, por padrão, imutáveis. Uma vez que um objeto record é criado, seus valores não podem ser alterados. Isso é útil para garantir que os dados não sejam acidentalmente modificados após a criação do objeto.
Menos código:

Ao declarar uma record, você não precisa escrever métodos como construtores, getters, equals(), hashCode(), e toString() manualmente. O compilador gera esses métodos automaticamente com base nos campos que você define.
Sintaxe:

A declaração da record inclui a definição de seus campos diretamente na assinatura, e o compilador cria automaticamente os métodos necessários.


O que o compilador gera?
Ao usar uma record, o compilador Java automaticamente cria:

Um construtor que aceita id e amount como parâmetros.
Métodos getId() e getAmount() para acessar os valores dos campos.
Implementações de equals(), hashCode(), e toString(), que são úteis para comparar objetos e representá-los como texto.
*/