package Emprestimos;

import java.time.LocalDate;

public interface Emprestimo {
    void verificaUsuario(String cpf, String agencia); //essa verificacao faz jus ao
    void OferecerEmprestimo(String nome, String cpf, double renda, String negativo, int score, String agencia); //esse puxa os dados do db_nacional
    void enviarEmprestimo(String cpf, double valor_emprestimo, double valor_total, LocalDate data, LocalDate data_vencimento, String agencia);
    void devedor_cliente(String cpf, double valor_total, String data, String vencimento);
}