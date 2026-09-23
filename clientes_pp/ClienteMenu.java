package Emprestimos.clientes_pp;

import java.util.Scanner;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;

import java.time.LocalDate;

import Emprestimos.EmprestimosApp;
import Emprestimos.databases.*;

public class ClienteMenu {
    public EmprestimosApp app = new EmprestimosApp();
    private String nome;
    private String cpf;
    private String conta_cc;
    private Boolean tem_emprestimo = false;

    public ClienteMenu(String _nome, String _cpf, String _ag) {
        this.nome = _nome;
        this.cpf = _cpf;
        this.conta_cc = _ag;
    }

    //getters
    public String get_nome() {
        return nome;
    }
    public String get_cpf() {
        return cpf;
    }
    public String get_agencia() {
        return conta_cc;
    }

    public void menu_usuario(String nome, String cpf, String agencia) {
        @SuppressWarnings("resource")
        Scanner scn = new Scanner(System.in);
        tem_emprestimo(cpf);

        if(!tem_emprestimo) {
            System.out.println("OLA, " + nome + "\n");
            System.out.println("[1] --- SOLICITAR EMPRESTIMO");

            int opt = 0;
            while(true) {
                opt = scn.nextInt();

                if(opt <= 0|| opt > 2) {
                    System.out.println("Opcao nao existe!");
                } else {
                    break;
                }
            }

            switch(opt) {
                case 1:
                System.out.println("---OFERTA DISPONIVEL---");
                EmprestimosApp app = new EmprestimosApp();
                app.verificaUsuario(cpf,agencia);
                break;
            }
        }
    }

    public void tem_emprestimo(String cpf) { //Aqui faz uma verificacao se ha algum emprestimo 
        String query = "SELECT cpf, valor_emprestimo, valor_juros, data_pegado, data_prazo FROM tbl_emprestimo WHERE cpf = ?"; //aqui faz uma query para o tbl_emprestimo, se existe, tem emprestimo

        @SuppressWarnings("resource")
        Scanner scn = new Scanner(System.in);
        //app.verifica_data();
        Database_e banco = new Database_e();
        Connection cnn = null;
        try {
            cnn = banco.conecao_database("emprestimos");
            PreparedStatement verificar = cnn.prepareStatement(query);

            verificar.setString(1,cpf);
            ResultSet rs = verificar.executeQuery();
            if(rs.next()) {
                double valor_emprestado = rs.getDouble("valor_emprestimo");
                double valor_com_j = rs.getDouble("valor_juros");
                String data_que_pegou = rs.getString("data_pegado");
                String vencimento = rs.getString("data_prazo");

                System.out.println("VALOR: " + valor_emprestado + " VALOR TOTAL: " + valor_com_j + " DATA: " + data_que_pegou + " VENCIMENTO: " + vencimento);
                app.verifica_data(cpf,valor_com_j,LocalDate.parse(data_que_pegou), LocalDate.parse(vencimento));
                this.tem_emprestimo = true;

                System.out.println("[1] - PAGAR | [2] - SAIR");
                int opt = scn.nextInt();

                if(opt > 2) {
                    System.out.println("Voce e gay amigo");
                } else if(opt==1){
                    System.out.print("VALOR >>> ");
                    double pagamento = scn.nextDouble();
                    pagar_emprestimo(cpf,pagamento,valor_com_j);
                }
            } else {
                System.out.println("Voce nao possui emprestimo ativo");
            }
        } catch(SQLException e) {
            System.out.println("Erro no servidor: " + e);
        }
    }

    public void pagar_emprestimo(String cpf, double pagamento, double valor) {
        //valor = valor do emprestimo contabilizando os juros
        if(pagamento == valor) { //checando se o valor do pagamento e igual ao valor do em.
            String remover_divida_1 = "DELETE FROM tbl_emprestimo WHERE cpf = ?";
            String remover_divida_2 = "DELETE FROM tbl_devedores WHERE cpf = ?"; 

            Database_e database = new Database_e();
            Connection conecao = null;

            int linhas = 0;
            try {
                conecao = database.conecao_database("emprestimos");
                PreparedStatement remover_emprestimo = conecao.prepareStatement(remover_divida_1); 
                remover_emprestimo.setString(1,cpf);
                linhas = remover_emprestimo.executeUpdate(); 
            } catch(SQLException e) {
                System.out.println("Erro no servidor: " + e);
            } 

            System.out.println("Removendo divida");
            try {
                conecao = database.conecao_database("emprestimos");
                PreparedStatement remover_devedor = conecao.prepareStatement(remover_divida_2);
                remover_devedor.setString(1,cpf);
                linhas = remover_devedor.executeUpdate();
            } catch(SQLException e) {
                System.out.println("Erro no servidor: " + e);
            }

            if(linhas > 0) {
                System.out.println("Pagamento setado com sucesso!");
            } else {
                System.out.println("Sistema nao pode concluir o pagamento!");
            }
        } else if(pagamento > valor || pagamento < valor) {
            System.out.println("Valor menor ou maior");
        }
    }
}