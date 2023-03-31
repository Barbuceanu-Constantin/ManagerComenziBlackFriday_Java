import java.util.concurrent.Semaphore;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileReader;
import java.io.File;

public class ThreadCitireProduse extends Thread {
    private int nrProcessedProducts;
    private BufferedWriter brw;
    private String commandId;
    private String fileName;
    private Semaphore sem;
    private Semaphore sem_out;
    private int id;

    public ThreadCitireProduse (int id, String fileName, BufferedWriter brw,
                                String commandId, int nrProcessedProducts,
                                Semaphore sem, Semaphore sem_out) {
        this.nrProcessedProducts = nrProcessedProducts;
        this.commandId = commandId;
        this.fileName = fileName;
        this.brw = brw;
        this.sem = sem;
        this.id = id;
        this.sem_out = sem_out;
    }
    
    public void run() {
        File productFile = new File(fileName);
        BufferedReader br =  null;
        String line = null;
        int i = 1;
        
        /*
            Fiecare thread worker va avea BufferedReaderul lui,
            dar se va opri la un alt produs din comanda in mod
            incremental. Adica un worker pornit dupa un altul
            va cauta urmatorul produs din comanda.
        */
        try {
            br = new BufferedReader(new FileReader(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
            Se citeste o linie la fiecare pas al whileului, dar
            se incrementeaza i-ul doar cand s-a gasit un produs
            din comanda. readLine e thread-safe;
        */
        while(i <= nrProcessedProducts) {
            try {
                line = br.readLine();
                if(line == null) break;
                String[] tokens = line.split(",");
                if(tokens[0].equals(commandId)) {
                    ++i;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        /*
            Fiecare thread worker de prelucrare produs scrie
            intr-un mutex linia din fisierul de produse,
            ca sa nu scrie in acelasi timp doua threaduri.
            In plus adauga statusul "shipped".
            sem este practic un mutex implementat printr-un
            semafor de capacitate 1.
        */
        if(line != null) {
            try {
                sem.acquire();
                brw.write(line.concat(",shipped\n"));
                sem.release();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }

        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        sem_out.release();
    }
}