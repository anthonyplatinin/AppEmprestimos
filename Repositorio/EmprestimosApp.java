package Emprestimos.Repositorio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

import Emprestimos.Clientes_CadastroLogin.*;
import Emprestimos.databases.Database_e;

public class EmprestimosApp implements Emprestimo {   
    private String querys_emprestimo; //contabilizacao dos juros
    public Connection cnn = null;
    public Database_e database_inicializer = new Database_e(); 

    public void set_query(String _query) {
        this.querys_emprestimo = _query;
    }
    public String get_query() {
        return querys_emprestimo;
    }

    @Override
    public void verificaUsuario(String cpf, String agencia) { // aqui ele vai fazer a requisicao ao db nacional
        Database_e database_inicializer = new Database_e();
        set_query("SELECT nome, renda, positivo_negativo, score FROM tbl_populacao WHERE cpf = ?");
 
        try {
            cnn = database_inicializer.conecao_database("nacional"); //conecao setado para o db nacional
            PreparedStatement pr = cnn.prepareStatement(get_query());
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
            System.out.println("Cpf Negativado!"); //Aqui vou botr apenas pra imprimir, depois seto outra funcao
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
            juros = 0.03; //1 por cento
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
    }

    @Override //se ele esta enviando entao nao esta pago
    public void enviarEmprestimo(String cpf, double valor_emprestimo, double valor_total, LocalDate data, LocalDate data_vencimento, String agencia) {
        set_query("INSERT INTO tbl_emprestimo (cpf,valor_emprestimo,valor_juros,data_pegado,data_prazo,status,agencia) VALUES (?,?,?,?,?,?,?)");
 
        int linhas = 0;
        try {
            cnn = database_inicializer.conecao_database("emprestimos"); //aqui ele vai pra db_emprestimo
            PreparedStatement pr = cnn.prepareStatement(get_query());
            pr.setString(1,cpf);
            pr.setDouble(2,valor_emprestimo);
            pr.setDouble(3,valor_total);
            pr.setString(4,data.toString());
            pr.setString(5,data_vencimento.toString());
            pr.setString(6, "NAO PAGO");
            pr.setString(7,agencia); 

            linhas = pr.executeUpdate();
            if(linhas > 0) { 
                System.out.println("Emprestimo Enviado seu pix caira em breve");
                verifica_data(cpf,valor_total,data,data_vencimento);  
                devedor_cliente(cpf,valor_total,data,data_vencimento,"NAO PAGO"); 
            } else {
                System.out.println("Valor nao enviado");
            }
        } catch(SQLException e) {
            System.out.println("Erro no servidor!" + e);
        }
    }
    
    ///AQUI FICA A PARTE E DIVIDAS, CONTAGEM E TAXAS, DEVEDORES ETC
 
    @Override
    public void verifica_data(String cpf, double valor_total, LocalDate data, LocalDate vencimento) {
        if(data.isAfter(vencimento)) { //se data e depois de vencimento
            System.out.println("[ALERTA] ESSE PAGAMENTO VENCEU"); //aqui ele cobrara uma taxa a mais
            esta_devendo(cpf,valor_total,vencimento); // aqui se venceu ele joga pra outro ngc 
        } else if(data.isEqual(vencimento)) {
            System.out.println("[ALERTA] Sua parcela vence hoje, pague e evite juros");
        } 
    }  

    //Inclui a funcao para ele enviar diretamente na tabela de devedores 
    @Override  
    public void devedor_cliente(String cpf, double valor_total, LocalDate data, LocalDate vencimento, String status) {
        set_query("INSERT INTO tbl_devedores (cpf, valor_juros, data, vencimento,status,ultima_att) VALUES (?,?,?,?,?,?)"); 
 
        int linhas = 0;
        LocalDate ultima_atualizacao = LocalDate.now(); 
        long dias_atrasados = ChronoUnit.DAYS.between(vencimento,data); //pega a [data] 24 - 27 [vencimento]
        try {    
            cnn = database_inicializer.conecao_database("emprestimos");
            PreparedStatement pp = cnn.prepareStatement(get_query());
            
            pp.setString(1,cpf);
            pp.setDouble(2,valor_total);
            pp.setString(3,data.toString());
            pp.setString(4,vencimento.toString());
            pp.setString(5,status);
            pp.setString(6,ultima_atualizacao.toString()); //a ultima att e quando ele sera enviado a tbl_devedores
            pp.executeUpdate(); 
        }catch(SQLException e) {
            System.out.println(e);
        }
    } 
 
    @Override
    public void esta_devendo(String cpf, double valor_juros, LocalDate vencimento) { //aqui ele e chamado quando o pagamento vence  
        set_query("SELECT ultima_att FROM tbl_devedores WHERE cpf = ?"); //aqui ele pega a ultima att
        LocalDate hoje = LocalDate.now(); //pega os dias de atraso 

        try {
            cnn = database_inicializer.conecao_database("emprestimos");
            PreparedStatement pr = cnn.prepareStatement(get_query());
            pr.setString(1,cpf); //pegando a ultima att

            ResultSet rs = pr.executeQuery();
            int linhas=0;
            if(rs.next()) {
                //aqui pegamos a ultima atualizacao
                //e verificamos se e compativel com o dia de hoje
                String ultima_atualizacao = rs.getString("ultima_att");

                //aqui calcula se ja houve alteracao no bds;

                long calculo = ChronoUnit.DAYS.between(LocalDate.parse(ultima_atualizacao),hoje);
                if(calculo > 0) { 
                    double taxa_atraso = calculo * 5; //aqui ele calcula desde a ultima att
                    double total = taxa_atraso + valor_juros;

                    set_query("UPDATE tbl_devedores SET valor_juros = ?, ultima_att = ? WHERE cpf = ?");
                    PreparedStatement state = cnn.prepareStatement(get_query());
                    state.setDouble(1,total);
                    state.setString(2,hoje.toString()); //nova att
                    state.setString(3,cpf);
                    linhas = state.executeUpdate();

                    if(linhas > 0) {
                        set_query("UPDATE tbl_emprestimo SET valor_juros = ? WHERE cpf = ?");
                        PreparedStatement st = cnn.prepareStatement(get_query());
                        st.setDouble(1,total);
                        st.setString(2,cpf);
                        st.executeUpdate();
                    }
                } else {
                    System.out.println("Sistema ja atualizado por hoje");
                }
            }
        
        } catch (SQLException e) {
            System.out.println("Erro no servidor: " + e);
        }
    }
}