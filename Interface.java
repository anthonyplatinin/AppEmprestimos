package Emprestimos;

import java.util.Scanner;
import Emprestimos.clientes_pp.ClienteMenu;

class Interface {
    public static void main(String[] args) { //ESSA PARTE E TEMPORARIA
        Scanner scn = new Scanner(System.in);

        ClienteMenu cliente = new ClienteMenu("Anthony","123.123.123-12", "Nubank 550-5");

        String nome_get = cliente.get_nome();
        String cpf_get = cliente.get_cpf();
        String agencia = cliente.get_agencia();

        cliente.menu_usuario(nome_get,cpf_get,agencia);
        scn.close();
    }
}