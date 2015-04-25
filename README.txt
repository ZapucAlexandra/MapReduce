***Nume: Zapuc Ileana-Alexandra
***Grupa: 331CA
***Tema2 APD: MapReduce

_______________________________


	CompareTask - un obiect de tip CompareTask reprezinta un task compare ce
va fi adaugat intr-un WorkPool de compare. Task-urile compare sunt create dupa
terminarea tuturor task-urilor reduce fiind create perechi de fisiere astfel 
fisierul 1 este diferit de fisierul 2.


________________________________


	MapTask - un obiect de tip MapTask reprezinta un task map ce va fi adaugat
intr-un WorkPool de map.

________________________________


	ReduceTask - un obiect de tip ReduceTask reprezinta un task reduce ce va fi
adaugat intr-un WorkPool de reduce. Task-urile sunt create dupa terminarea celor
de tip map.

________________________________

	Task - este o interfata care are rolul de a face legatura intre cele trei 
task-uri. Ajuta si la compactarea codului.


________________________________

	WorkPool -- fisierul din laborator.
	
	
________________________________

	ReplicatedWorkers - Dupa ce se face citirea fisierului de intrare, se incepe
crearea task-urilor de tip Map prin impartirea fisierelor in fragmente cu offset
si dimensiune.	Dupa pornirea si executia thread-urilor, se vor crea task-urilor
de tip Reduce. In final, se va obtine un rezultat dupa executia task-urilor de
tip Compare care consta intr-o asociere intre "Nume fisier 1; Nume fisier 2" si 
similaritatea dintre cele doua.  Rezultatul este sortat descrescator dupa 
similaritate si apoi sunt afisate cele care au similaritatea mai mare decat un
grad maxim.
	
	
_________________________________

	Worker - In clasa worker, exista trei metode de procesare a task-urilor care
ajuta workerii in executia lor:
			1. processMapTask
			2. processReduceTask
			3. processCompareTask
	1. In processMapTask este stabilit fragmentul pe care un worker il prelu - 
creaza avand grija ca inceputul si sfarsitul fragmentului sa nu se afle in 
interiorul unui cuvant. Toate cuvitele sunt retinute intr-un hash.
	2.In processReduceTask sunt  accesate toate hash-urile pentru un anumit 
fisier, care apoi vor fi combinate pentru a crea un hash pentru intregul fisier.
	3.In processCompareTask, pentru perechea de fisire curenta, este calculata
similaritatea.
