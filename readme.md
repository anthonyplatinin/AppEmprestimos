Aplicativo que simula uma agencia de emprestimos (Super Facil)
no modelo mais simples que eu encontrei

Como ele funciona

Ha um banco de dados chamado "db_nacional"
Nele ha informacoes como, nome, cpf, score, se o cpf esta negativo ou nao
Essa informacoes servem como se fossem uma API para o aplicativo funcionar
funciona basicamente como um gov.br, um app nacional que guarda as informacoes
da população.

O app começa com o usuario criando a conta, digitando seu nome,cpf e agencia
dito isso ele sera cadastrado no sistema tbl_usuario na database db_emprestimos
com isso, quando o usuario inicia ele perguna se quer solicitar um emprestimo
nisso o banco de dados puxa os dados cadastrados no db_nacional para fazer uma
analise de perfil no individuo (Como se fosse uma api do gov.br)

Se ele estiver com o cpf negativado o app nao libera os emprestimos
Se ele estiver com o score baixo,medio,alto o juros pode ficar grande ou pequeno

Nova versão!!! [21/09/2026]

Bug do SQL truncated consertado
Funcao para enviar para a tabela de dividas (tbl_devedores) <---- irei remover pois acho que nao tem necessidade. [23-09-26]
Funcao de pagamento atualizada.
Funcao de verificacao da data atualizada <---- Tava funcionando mas fiz cagada

Dashboard do dono ainda nao concluido, nem iniciado

<<<Quando for iniciarliza-lo>>> 
javac -cp ".;pastadomysql.jar" *.java databases/*.java clientes_pp/*.java painel_dono/*.java
Executar: java -cp ".;pastadomysql.jar" Interface.java

E necessario ter o mysql-connector-8-3-0.jar instalado
E criar as databases no xampp

Proximas att
1. Refazer a função atualizar os juros
2. Fazer uma funcao para atualizar os dias [E se ele chegar perto da data de vencer, ele avisa o cliente]
3. Reestrutura o codigo, com o codigo em comentario
4. Fazer a logica do Cliente [Criar conta e logar]
5. fazer o dashboard do dono