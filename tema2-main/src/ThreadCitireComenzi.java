import java.util.concurrent.Semaphore;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;

public class ThreadCitireComenzi extends Thread {
    private BufferedWriter br_product_output;
    private BufferedWriter br_order_output;
    private BufferedReader br_order_input;
    private boolean fileNotFound;
    private String fileName;
    private Semaphore sem1;
    private Semaphore sem2;
    private Semaphore sem;
    private int id;
    private int P;

    public boolean getFileNotFound() {
        return fileNotFound;
    }

    public ThreadCitireComenzi( int id, BufferedReader br_order_input, String file,
                                BufferedWriter br_order_output, BufferedWriter br_product_output,
                                Semaphore sem, int P, Semaphore sem1, Semaphore sem2) {
        this.br_product_output = br_product_output;
        this.br_order_output = br_order_output;
        this.br_order_input = br_order_input;
        this.fileNotFound = false;
        this.fileName = file;
        this.sem1 = sem1;
        this.sem2 = sem2;
        this.sem = sem;
        this.id = id;
        this.P = P;
    }

    public void run() {
        String line;
        while(true) {
            try {
                /*
                    Fiecare thread de prelucrare comenzi citeste
                    intr-un mutex linia din fisierul de comenzi,
                    ca sa nu o citeasca pe aceeasi simultan.
                    readLine este thread-safe.
                */
                line = br_order_input.readLine();
                if(line == null) break;

                //Se face split pe linie ca sa se extraga nr-ul de produse si id-ul comenzii.
                String[] tokens = line.split(",");
                int nrOfProducts = Integer.parseInt(tokens[1]);
                ThreadCitireProduse[] t = new ThreadCitireProduse[nrOfProducts];
                int nrProcessedProducts = 1;
                int i = 0;
                /*
                    In acest while startez nrOfProducts threaduri worker
                    fiecare urmand sa se ocupe de cautarea unui produs
                    din comanda. Fiecare thread worker de nivel2 va da
                    release la finalul sau pe semaforul primit ca parametru
                    de constructor.
                    "nrProcessedProducts" este o variablia contor care
                    specifica la al catelea produs din comanda va trebui sa
                    se opreasca threadul worker din cautare.
                */
                while (i < nrOfProducts) {
                    try {
                        sem.acquire();
                        /*
                            i = indexul threadului; tokens[0] = id comanda;
                            br2 = bufferedReaderul din care se va citi.
                        */
                        t[i] = new ThreadCitireProduse(i, fileName, br_product_output, tokens[0], nrProcessedProducts, sem2, sem);
                        t[i].start();
                        ++i;
                        ++nrProcessedProducts;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                //Atept si threadurile ramase.
                for(i = 0; i < nrOfProducts; ++i) {
                    try {
                        t[i].join();
                        sem.release();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                /*
                    Fiecare thread de prelucrare comenzi scrie
                    intr-un mutex linia din fisierul de comenzi,
                    ca sa nu scrie in acelasi timp doua threaduri.
                    In plus adauga statusul "shipped".
                    "sem1" este un mutex implementat printr-un semafor
                    de capacitate 1.
                */
                if(nrOfProducts != 0) {
                    try {
                        sem1.acquire();
                        br_order_output.write(line.concat(",shipped\n"));
                        sem1.release();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                this.fileNotFound = true;
                break;
            }
        }
    }
}
