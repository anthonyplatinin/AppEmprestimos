package Emprestimos.databases;

//aqui ele apenas faz a conecao A database desse app

import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.Connection;

public class Database_e {
    public Connection conecao_database(String database) throws SQLException {
        String host = "root";
        String pass = "";
        String url = "";

        if(database.equals("nacional")) {
            url = "jdbc:mysql://localhost:3306/db_nacional"; //aqui guarda as infos desse app
        }
        if(database.equals("emprestimos")) {
            url = "jdbc:mysql://localhost:3306/db_emprestimos";
        }

        Connection conn = null; //aqui vai conectar a nacional e vai funcionar como api
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url,host,pass);

            return conn;
        } catch(Exception e) {
            System.out.println("Erro nos servidores [Exception]" + e);
            throw new RuntimeException("Error" + e);
        }
    }
}
