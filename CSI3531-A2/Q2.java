// Étudiant 1: Michael Massaad, Code Étudiant: 300293612
// Étudiant 2: Gabriel Zohrob, Code Étudiant: 300309391

import java.util.Scanner;
class Fibonacci implements Runnable{
    private int[] nombres;
    private int len;
    
    // Constructeur
    public Fibonacci(int[] mem, int length){
        this.nombres = mem;
        this.len = length;
    }

    // Implementation de la methode run de Runnable
    public void run(){
        if(len>0){
            nombres[0] = 0;
        } 
        if (len>1){
            nombres[1] = 1;
        }

        for(int i=2; i<len;i++){
            nombres[i]=nombres[i-1]+nombres[i-2];
        }
    }
}
public class Q2{

    public static void main(String args[]){
        // Obtennir l'entre de l'usager
        Scanner number = new Scanner(System.in);
        System.out.print("Entrer le nombre de nombres de Fibonacci que tu veut generer: ");
        int num = Integer.parseInt(number.nextLine());

        // On ne peut pas avoir entree negative
        if(num<0){
            throw new IllegalArgumentException("Le nombre doit être positif.");
        }

        int[] nombres = new int[num]; // Creation de memoire partagee par tableau

        // Creation de fil
        Thread fib = new Thread(new Fibonacci(nombres, num));
        fib.start(); // Declenchement du fil
        try {
            fib.join(); // Parent fil attend que le fil enfant termine
        } catch (InterruptedException e) { }

        // Apres que le fil enfant termine, on imprime le contenu du memoire partagee
        if(num == 0){
            System.out.println("Aucun nombre de Fibonacci à afficher.");
        } else{
            System.out.println("Les "+num+" premiers nombres de Fibonacci est: ");
            for(int i=0; i<num;i++){
                System.out.print(nombres[i]+" ");
            }
            System.out.print("\n");
        }
    }
}