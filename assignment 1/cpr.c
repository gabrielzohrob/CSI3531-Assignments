/*------------------------------------------------------------
Fichier: cpr.c

Nom: Gabriel Zohrob, Michael Massaad
Numero d'etudiant: 300309391, 300293612

Description: Ce programme contient le code pour la creation
             d'un processus enfant et y attacher un tuyau.
	     L'enfant envoyera des messages par le tuyau
	     qui seront ensuite envoyes a la sortie standard.

Explication du processus zombie
(point 5 de "A completer" dans le devoir):

	Quand un processus termine et que tous ses 
	ressources ne sont pas encore relachées, il est 
	en dans l'état "Zombie".

	Dans notre programme, quand l'enfant termine,
	il envoie un signal au parent, mais comme le
	parent n'utilise pas wait(), les informations
	de l'enfantne sont pas récupérées. Pendant les 
	10 secondes de sleep(), on peut voir l'enfant
	en état zombie.
-------------------------------------------------------------*/
#include <stdio.h>
#include <sys/select.h>
#include <unistd.h>
#include <stdlib.h>

/* Prototype */
void creerEnfantEtLire(int );

/*-------------------------------------------------------------
Function: main
Arguments: 
	int ac	- nombre d'arguments de la commande
	char **av - tableau de pointeurs aux arguments de commande
Description:
	Extrait le nombre de processus a creer de la ligne de
	commande. Si une erreur a lieu, le processus termine.
	Appel creerEnfantEtLire pour creer un enfant, et lire
	les donnees de l'enfant.
-------------------------------------------------------------*/

int main(int ac, char **av)
{
    int numeroProcessus; 

    if(ac == 2)
    {
       if(sscanf(av[1],"%d",&numeroProcessus) == 1)
       {
           creerEnfantEtLire(numeroProcessus);
       }
       else fprintf(stderr,"Ne peut pas traduire argument\n");
    }
    else fprintf(stderr,"Arguments pas valide\n");
    return(0);
}


/*-------------------------------------------------------------
Function: creerEnfantEtLire
Arguments: 
	int prcNum - le numero de processus
Description:
	Cree l'enfant, en y passant prcNum-1. Utilise prcNum
	comme identificateur de ce processus. Aussi, lit les
	messages du bout de lecture du tuyau et l'envoie a 
	la sortie standard (df 1). Lorsqu'aucune donnee peut
	etre lue du tuyau, termine.
-------------------------------------------------------------*/

void creerEnfantEtLire(int prcNum)
{
	//CAS DE BASE: Nous avons un seul processus ie. processus 1
    if(prcNum == 1)
	{
		fprintf (stderr, "Processus 1 commence\n");
		sleep(5);
		fprintf (stderr, "Processus 1 termine\n");
		sleep(10);
		return;
	}

	//printf("Processus %d commence\n", prcNum);
	int pipes[2], tuy, pid;
	tuy = pipe(pipes);

	//error handling pipe
	if (tuy == -1)
	{
		fprintf(stderr,"Erreur dans la creation du tuyau\n");
		exit(-1);
	}

	pid = fork();
	//error handling fork
	if (pid < 0)
	{
		fprintf (stderr, "Erreur dans la creation du processus enfant\n");
		exit(-1);
	}

	//child process
	else if (pid == 0)
	{
		// close the read
		close(pipes[0]);
		
		//Redirect stdout to write end of the pipe
		int dp2= dup2(pipes[1], 1);
		if (dp2 == -1)
		{
			fprintf(stderr, "Erreur lors de la dup");
			exit(-1);
		}

		//close the write-end 
		close(pipes[1]);

		//re-executing the program
		char strNum[12];
		sprintf(strNum, "%d", prcNum - 1);
		char *args[] = {"./cpr", strNum, NULL};
		execvp(args[0], args);

		// If execvp fails
		perror("Erreur lors de l'execution de execvp");
		exit(-1);
	}

	//parent process
	else
	{
		//close the write-end of the pipe
		close(pipes[1]);

		//parent proccess starts
		fprintf(stderr, "Processus %d commence\n", prcNum);

		//read from the buffer
		char buffer[1024];
        ssize_t n;
        while ((n = read(pipes[0], buffer, sizeof(buffer))) > 0)
        {
            write(1, buffer, n); // Print to own stdout
        }

		//closing the read end
		close(pipes[0]);

		// goes to the terminal if original process is reached, goes into the pipe if it is someone else's child
		fprintf(stderr, "Processus %d termine\n", prcNum);

		sleep(10);
	}
}