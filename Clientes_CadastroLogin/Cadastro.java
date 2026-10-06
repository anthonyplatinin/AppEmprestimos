package Emprestimos.Clientes_CadastroLogin;

import java.sql.*;
import java.util.Scanner;
import Emprestimos.databases.*;

//para simplifcar um pouco, o score tera valores fixos

public class Cadastro {
    private String nome;
    private String cpf;
    private String agencia;
    private Double renda_mensal;

    void set_renda(Double rndm) {
        this.renda_mensal = rndm;
    }

    void set_nome(String n) {
        this.nome= n;
    }
    void set_cpf(String c) {
        this.cpf = c;
    }
    void set_agencia(String a) {
        this.agencia = a;
    }

    String get_nome() {
        return nome;
    }
    String get_cpf() {
        return cpf;
    }
    String get_agencia() {
        return agencia;
    }
    Double get_renda() {
        return renda_mensal;
    }

    void existe_usuario(String cpf) {
        String query = "SELECT cpf FROM tbl_usuario WHERE cpf = ?";
        Connection conecao = null;
        Database_e db = new Database_e();

        try {
            conecao = db.conecao_database("emprestimos");
            PreparedStatement pr = conecao.prepareStatement(query);
            pr.setString(1,cpf);

            ResultSet rs = pr.executeQuery();
            if(rs.next()) {
                System.out.println("Usuario ja cadastrado!");
            } else {
                setDados(get_nome(), get_cpf(), get_agencia(), get_renda());               
            }
        } catch(Exception e) {
            System.out.println("Erro no [CADASTRO] -> " + e);
        }

    }

    public void criarConta() {
        System.out.println("==== DIN FACIL ====");
        System.out.println("CADASTRAR");
        System.out.println("[1 NOME COMPLETO] [2 CPF] [3 AGENCIA] [4 RENDA MENSAL]");
        Scanner scn = new Scanner(System.in);

        String nome_cliente = scn.nextLine();
        String cpf_cliente = scn.nextLine();
        String agencia_cliente = scn.nextLine(); 
        Double renda_cliente = scn.nextDouble();

        set_nome(nome_cliente);
        set_cpf(cpf_cliente);
        set_agencia(agencia_cliente);
        set_renda(renda_cliente);

        existe_usuario(cpf);
    }
    void setDados(String nome, String cpf, String agencia, Double renda_mensal) {
        String inserir = "INSERT INTO tbl_usuario (nome,cpf,agencia,renda_mensal,score) VALUES (?,?,?,?,?)";

        Connection cnn = null;
        Database_e db = new Database_e();
        try {
            cnn = db.conecao_database("emprestimos");
            PreparedStatement inserir_usuario = cnn.prepareStatement(inserir);
            
            inserir_usuario.setString(1,nome);
            inserir_usuario.setString(2,cpf);
            inserir_usuario.setString(3,agencia);
            inserir_usuario.setDouble(4,renda_mensal);

            int score = 0;
            if(renda_mensal <= 1500) {
                score = 250;
            } 
            if(renda_mensal <= 3000) {
                score = 400;
            } 
            if(renda_mensal <= 5000) {
                score = 550;
            } 
            if(renda_mensal >= 10000) {
                score = 700;
            }
            inserir_usuario.setInt(5,score);
        
            int exec = inserir_usuario.executeUpdate();
            if(exec > 0) {
                System.out.println("Muito obrigado por realizar o cadastro!");
            }
        }catch(Exception e) {
            System.out.println("Erro no cadastro linha 117 -" + e);
        }
    }

    public void entrarConta() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("[1 CPF]");
        String cpf = scanner.nextLine();

        ClienteMenu cliente = ClienteMenu();
        cliente.menu_usuario(get_nome(),cpf,get_agencia());
    }
}