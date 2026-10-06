package Emprestimos.Clientes_CadastroLogin;

import java.util.Scanner;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;

import java.time.LocalDate;

import Emprestimos.Repositorio.*;
import Emprestimos.databases.*; 

public class ClienteMenu {
    EmprestimosApp app = new EmprestimosApp(); 
    private Boolean tem_emprestimo = false;  

    //Aqui e o menu se nao possui emprestimo
    public void menu_usuario(String nome, String cpf, String agencia) {
        @SuppressWarnings("resource")
        Scanner scn = new Scanner(System.in);
        tem_emprestimo(cpf);

        if(!tem_emprestimo) {
            System.out.println("OLA, " + nome + "\n");
            System.out.println("[1] --- SOLICITAR EMPRESTIMO");
            System.out.println("[2] --- SAIR");
  
            while(true) {
                int opt = scn.nextInt();
                scn.nextLine();

                switch(opt) {
                    case 1:
                        System.out.println("---OFERTAS DISPONIVEL---"); 
                        app.verificaUsuario(cpf,agencia);
                        break;
                    case 2:
                        System.out.println("ATE LOGO");
                        break;
                    default:
                        System.out.println("Opcao nao existe!");
                        break;
                }
            }
        }
    }

    //Aqui ele verifcia se tem emprestimo ativo
    public void tem_emprestimo(String cpf) {   
        String query = "SELECT cpf, valor_emprestimo, valor_juros, data_pegado, data_prazo FROM tbl_emprestimo WHERE cpf = ?";

        @SuppressWarnings("resource")
        Scanner scn = new Scanner(System.in); 
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

                System.out.println("[1] PAGAR  [2] SAIR");
                int opt = scn.nextInt();

                if(opt > 2) {
                    System.out.println("SAINDO...");
                } else if(opt == 1) {
                    System.out.print("VALOR >>> ");
                    
                    while(true) {
                        double pagamento = scn.nextDouble();
                        if(valor_com_j < pagamento || valor_com_j > pagamento) {
                            System.out.println("TENTE NOVAMENTE!");
                        } else {
                            pagar_emprestimo(cpf,pagamento,valor_com_j);
                            break;
                        } 
                    }
                }
            } else {
                System.out.println("Voce nao possui emprestimo ativo");
            } 
        } catch(Exception e) {
            System.out.println("Erro no [Cliente-Menu]" + e);
        }
    }  

    public void pagar_emprestimo(String cpf, double pagamento, double valor) {

        //1. PAGANDO ELE REMOVE DA TABELA PRINCIPAL
        //2. REMOVE TBM DE DEVEDORES
        //3. ADICIONA EM PAGADORES



        String query_Emprestimo = "DELETE FROM tbl_emprestimo WHERE cpf = ?"; 
        int linhas = 0;
        int dev = 0;
        Connection cnn = null;
        Database_e banco = new Database_e();

        try {
            cnn = banco.conecao_database("emprestimos");
            PreparedStatement atualizar = cnn.prepareStatement(query_Emprestimo);

            atualizar.setString(1,cpf);
            
            linhas = atualizar.executeUpdate();
            if(linhas > 0) {
                String query_devedores = "DELETE FROM tbl_devedores WHERE cpf = ?"; //DELETA DE DEVEDORES
                PreparedStatement novo_att = cnn.prepareStatement(query_devedores);
                novo_att.setString(1,cpf);

                dev = novo_att.executeUpdate();
                if(dev > 0) {
                    String query_inserir = "INSERT INTO tbl_pagadores (cpf,status,valor_juros) VALUES (?,?,?)";
                    PreparedStatement inserir_pagamento = cnn.prepareStatement(query_inserir);
                    inserir_pagamento.setString(1,cpf);
                    inserir_pagamento.setString(2,"PAGO");
                    inserir_pagamento.setDouble(3,valor);

                    int att = inserir_pagamento.executeUpdate();

                    if(att>0) {
                        System.out.println("Pagamento realizado!");
                    }
                }
            }
        } catch(Exception e) {
            System.out.println("Falha -> " + e);
        }
    }
}