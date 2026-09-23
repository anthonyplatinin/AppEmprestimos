
//TODA E QUALQUER ALTERAÇÃO QUANDO FOR COMPILAR USAR TODAS AS PASTAS!

package Emprestimos;

import java.sql.Connection;

//score tabela
//baixo - 300-500 - boa chance de aprovacao mas a quitacao e mais cara p/ compensar
//alto - 700-1000 - otima chance, juros baixos etc.

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

import Emprestimos.clientes_pp.ClienteMenu;
import Emprestimos.databases.Database_e;

///Aqui tera 3 fases do emprestimo 1 - NAO PAGO, 2 - DIVIDA, 3 - PAGO

public class EmprestimosApp implements Emprestimo {   
    private String querys_emprestimo; //contabilizacao dos juros
    //private Connection cnn = null;
    //private Database_e database_inicializer = new Database_e(); 

    public void set_query(String _query) {
        this.querys_emprestimo = _query;
    }
    public String get_query() {
        return querys_emprestimo;
    }


    @Override
    public void verificaUsuario(String cpf, String agencia) { // aqui ele vai fazer a requisicao ao db nacional
        Database_e nacional = new Database_e();
        String query = "SELECT nome, renda, positivo_negativo, score FROM tbl_populacao WHERE cpf = ?";

        Connection conecao = null;
        try {
            conecao = nacional.conecao_database("nacional"); //conecao setado para o db nacional
            PreparedStatement pr = conecao.prepareStatement(query);
            pr.setString(1,cpf);
            ResultSet rs = pr.executeQuery();

            double renda = 0.0;
            String nome = "";
            int score_usuario = 0;
            String negativo = "";
            if(rs.next()) {
                nome = rs.getString("nome");
                renda = rs.getDouble("renda");
                negativo = rs.getString("positivo_negativo");
                score_usuario = rs.getInt("score");

                OferecerEmprestimo(nome,cpf,renda,negativo,score_usuario,agencia);
            } else {
                System.out.println("Nao puxei nenhum dos dados!");
            }
        } catch(SQLException e) {
            System.out.println("Error no servidor: " + e);
        }
    }

    @Override
    public void OferecerEmprestimo(String nome, String cpf,double renda, String negativo, int score,String agencia) { //aqui ele recebe os dados e passa as info dos emprestimos
        if(negativo.equals("Negativo")) {
            System.out.println("Cpf Negativado!");

            ClienteMenu cc = new ClienteMenu(nome, cpf,agencia);
            cc.menu_usuario(nome,cpf,agencia);
        }

        //Vai pegar a pessoa pelo Score
        //tabelas de juros

        double maximo_cliente = 0; // ele pega a renda com 0,3%
        double juros_a_pagar = 0;
        double juros = 0;
        double total = 0.0;
        double multiplicador = 0;

        if(score >= 250 && score <= 450) {
            multiplicador = 0.15; //15 per cento da renda
            juros = 0.095; // 9,5
            maximo_cliente = renda * multiplicador;
        }
        else if(score > 450 && score <= 500) {
            multiplicador = 0.10; //10 per cento da renda
            juros = 0.035; // 3,5per cento
            maximo_cliente = renda * multiplicador;
        } else if(score > 500 && score <= 700) {
            multiplicador = 0.20; //20 per cento da renda
            juros = 0.050; //5 por cento
            maximo_cliente = renda * multiplicador;
        }
        else if(score > 700) {
            multiplicador = 0.05; //5 per cento da renda
            juros = 0.02; //1 por cento
            maximo_cliente = renda * multiplicador;
        }

        juros_a_pagar = maximo_cliente * juros;
        total = juros_a_pagar + maximo_cliente;
        LocalDate data_emprestimo = LocalDate.now();
        LocalDate data_vencimento = data_emprestimo.plusDays(30);

        System.out.println("Valor: " + String.format("%.2f",maximo_cliente));
        System.out.println("Total a pagar: " + String.format("%.2f",total));
        System.out.println("Pegando hoje: " + data_emprestimo);
        System.out.println("Vencimento: " + data_vencimento);

        System.out.println("[S] - [N]");
        Scanner scn = new Scanner(System.in);
        String opt = scn.nextLine();

        if(opt.equals("N") || opt.equals("n")) {
            System.out.println("Volte ao menu!");
        } else {
            enviarEmprestimo(cpf,maximo_cliente,total,data_emprestimo,data_vencimento, agencia); //cpf, quanto foi, quanto deve + data
        }
        scn.close();
    }

    @Override //se ele esta enviando entao nao esta pago
    public void enviarEmprestimo(String cpf, double valor_emprestimo, double valor_total, LocalDate data, LocalDate data_vencimento, String agencia) {
        String inserir_emprestimo = "INSERT INTO tbl_emprestimo (cpf,valor_emprestimo,valor_juros,data_pegado,data_prazo,status,agencia,juros_original) VALUES (?,?,?,?,?,?,?)";

        Database_e cnn = new Database_e();

        Connection conecao = null;
        int linhas=0;

        try {
            conecao = cnn.conecao_database("emprestimos"); //aqui ele vai pra db_emprestimo
            PreparedStatement pr = conecao.prepareStatement(inserir_emprestimo);
            pr.setString(1,cpf);
            pr.setDouble(2,valor_emprestimo);
            pr.setDouble(3,valor_total);
            pr.setString(4,data.toString());
            pr.setString(5,data_vencimento.toString());
            pr.setString(6, "NAO PAGO");
            pr.setString(7,agencia);
            pr.setDouble(8,valor_total); //esse e o valor dos juros original

            linhas = pr.executeUpdate();
            if(linhas > 0) { 
                System.out.println("Emprestimo Enviado seu pix caira em breve");
                verifica_data(cpf,valor_total,data, data_vencimento); 
                devedor_cliente(cpf, valor_total, data.toString(), data_vencimento.toString(),"NAO PAGO");
            } else {
                System.out.println("Valor nao enviado");
            }
        } catch(SQLException e) {
            System.out.println("Erro no servidor!" + e);
        }
    }

    ///AQUI FICA A PARTE E DIVIDAS, CONTAGEM E TAXAS, DEVEDORES ETC

    public void verifica_data(String cpf, double valor_total, LocalDate data, LocalDate vencimento) {
        if(data.isAfter(vencimento)) { //se data e depois de vencimento
            System.out.println("[ALERTA] ESSE PAGAMENTO VENCEU"); //aqui ele cobrara uma taxa a mais
            contagem_de_dias(vencimento, cpf, valor_total);
        } else if(data.isEqual(vencimento)) {
            System.out.println("[ALERTA] Sua parcela vence hoje, pague e evite juros");
        } 
    } 

    //FUNCAO PARA REFAZER AMANHA

    //refazer do 0
    /*public void contagem_de_dias(LocalDate vencimento, String cpf, double valor_total) {
        set_query("SELECT valor_juros,valor_original FROM tbl_emprestimo WHERE cpf = ?");

        try {

        } catch(SQLException e) {
            System.out.println("Erro o");
        }
    } */

    ///////////////////////////////////////////////
 
    @Override
    public void esta_devendo(String cpf, double valor_juros) { //aqui ele e chamado quando o pagamento vence 
        double valor_sobre_posto = valor_juros * 0.5;

        String update_divida = "UPDATE tbl_devedores SET valor_juros = ?, status = 'DIVIDA' WHERE cpf = ?";
        String update_tbl = "UPDATE tbl_emprestimo SET valor_juros = ?, status = 'DIVIDA' WHERE cpf = ?";

        Connection conecao = null;
        Database_e dts = new Database_e();
        int linha = 0;
        System.out.println("Ele nao amenta a divida por que ?");
        try {
            conecao = dts.conecao_database("emprestimos");
            PreparedStatement divida_dev = conecao.prepareStatement(update_divida);
            divida_dev.setString(1,cpf);
            linha = divida_dev.executeUpdate();
        } catch(SQLException e) {
            System.out.println("Erro no sistema: " + e);
        }

        try {
            conecao = dts.conecao_database("emprestimos");
            PreparedStatement prepare = conecao.prepareStatement(update_tbl);
            prepare.setString(1,cpf); //aqui ele envia para o tbl_emprestimos
            linha = prepare.executeUpdate();
        } catch(SQLException e) {
            System.out.println("Falha no servidor! " + e);
        }

        if(linha > 0) {
            System.out.println("Pague agora e evite juros"); 
        } else {
            System.out.println("Sistema idiota nao consegiu processar tua divida");
        }    
    }

    @Override
    public void devedor_cliente(String cpf, double valor_total, String data, String vencimento, String status) {
        String query = "INSERT INTO tbl_devedores (cpf, valor_juros, data, vencimento,status) VALUES (?,?,?,?,?)"; 
        Connection conecao = null; 
        int linhas = 0;

        try {   
            Database_e databases = new Database_e();
            conecao = databases.conecao_database("emprestimos");
            PreparedStatement pp = conecao.prepareStatement(query);
            
            pp.setString(1,cpf);
            pp.setDouble(2,valor_total);
            pp.setString(3,data);
            pp.setString(4,vencimento);
            pp.setString(5,status);
            
            linhas = pp.executeUpdate();
            if(linhas>0){
                System.out.println("Muito obrigado por fazer parte do time");
            } else {
                System.out.println("Nao foi possivel enviar os dados ao sistema");
            }
            
        }catch(SQLException e) {
            System.out.println(e);
        }
    }
}