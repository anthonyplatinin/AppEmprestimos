Aplicativo que simula uma agencia de emprestimos (Super Facil)
no modelo mais simples que eu encontrei

Como ele funciona

Ha um banco de dados chamado "db_nacional"
Nele ha informacoes como, nome, cpf, score, se o cpf esta negativo ou nao
Essa informacoes servem como se fossem uma API para o aplicativo funcionar
funciona basicamente como um gov.br, um app nacional que guarda as informacoes
da população.

Enfim, o app comeca com o usuario criando a conta, digitando seu nome,cpf,agencia
dito isso ele sera cadastrado no sistema tbl_usuario na database db_emprestimos
com isso, quando o usuario inicia ele perguna se quer solicitar um emprestimo
nisso o banco de dados puxa os dados cadastrados no db_nacional para fazer uma
analise de perfil no individuo

Se ele estiver com o cpf negativado o app nao libera os emprestimos
Se ele estiver com o score baixo,medio,alto o juros pode ficar grande ou pequeno

Ha funcao para checar a data do emprestimo se ela venceu ou não
funcao para pegar o pagamento e ele deleta a divida no db

Em breve tera o dashboar do dono, simula como se fosse o dono entrando no seu app
para ver transações ou gerenciar, etc.

Esse projeto esta sendo criado como uma forma de aprendizado de uma pessoa que parou
de estudar desenvolvimento e quer voltar, como era antes.