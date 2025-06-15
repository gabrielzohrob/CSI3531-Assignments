// Étudiant 1: Michael Massaad, Code Étudiant: 300293612
// Étudiant 2: Gabriel Zohrob, Code Étudiant: 300309391

import java.util.Scanner;

class Prime implements Runnable{
    private int n;

    // Methode pour savoir si un nombre n est premier
    static boolean isPrime(int n){
        if (n<=1){
            return false;
        }
        for(int i = 2; i<n; i++){
            if(n%i==0){
                return false;
            }
        }
        return true;
    }

    // Constructeur
    public Prime(int number){
        if(number<0){
            throw new IllegalArgumentException("Le nombre doit être positif.");
        }
        this.n = number;
    }

    // Implementation de la methode run de Runnable
    public void run(){
        System.out.println("Nombre premiers inferieurs ou egaux a "+ n +": ");
 
        for(int i = 1; i<=n; i++){ // Si le nombre est premier, on l'imprime
            if(isPrime(i)){
                System.out.println(i);
            }
        }
    }
}
public class Q1{

    public static void main(String args[]){
        // Obtennir l'entre de l'usager
        Scanner number = new Scanner(System.in);
        System.out.print("Entrer un nombre pour lequel on veut montrer les nombres premiers: ");
        int num = Integer.parseInt(number.nextLine());

        // Creation de fil
        Thread premiers = new Thread(new Prime(num));
        premiers.start(); // Declenchement du fil
        try {
            premiers.join(); // Parent fil attend que le fil enfant termine
        } catch (InterruptedException e) { }
    }
}