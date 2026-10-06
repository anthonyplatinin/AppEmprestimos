package Emprestimos;

import java.util.Scanner;
import Emprestimos.Clientes_CadastroLogin.*;

class Interface {
    public static void main(String[] args) { //ESSA PARTE E TEMPORARIA 
        System.out.println("=== BEM VINDO AO DIN FACIL ===");
        System.out.println("[1 CADASTRO] [2 LOGIN]");

        Cadastro cd = new Cadastro();
        
        Scanner scn = new Scanner(System.in);
        int opcoes = scn.nextInt();
        switch(opcoes) {
            case 1:
                cd.criarConta();
                break;
            case 2:
                cd.entrarConta();
                break;
            default:
                System.out.println("Essa opcao nao existe!");
                break;
        }
    }
}