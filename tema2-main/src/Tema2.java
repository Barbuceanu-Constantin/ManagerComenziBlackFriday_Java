import java.util.concurrent.Semaphore;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;

class Tema2{
    public static void main(String args[]) {
        String folder_input = args[0];
        int P = Integer.parseInt(args[1]);
        ThreadCitireComenzi[] t = new ThreadCitireComenzi[P];
        String commands_file = folder_input.concat("/").concat("orders.txt");
        String products_file = folder_input.concat("/").concat("order_products.txt");
        String commands_output = "./orders_out.txt";
        String products_output = "./order_products_out.txt";
        File order_file = new File(commands_file);
        File order_output = new File(commands_output);
        File product_output = new File(products_output);
        BufferedReader br_order_input = null;
        BufferedWriter br_order_output = null;
        BufferedWriter br_product_output = null;
        Semaphore sem = new Semaphore(P);
        Semaphore sem_write1 = new Semaphore(1);
        Semaphore sem_write2 = new Semaphore(1);

        try {
            br_order_input = new BufferedReader(new FileReader(order_file));
            br_order_output = new BufferedWriter(new FileWriter(order_output));
            br_product_output = new BufferedWriter(new FileWriter(product_output));            

            //Pornesc threadurile de nivel 1.
            for (int i = 0; i < P; ++i) {
                t[i] = new ThreadCitireComenzi(i, br_order_input, products_file, br_order_output, br_product_output, sem, P, sem_write1, sem_write2);
                t[i].start();
            }

            //Astept threadurile de nivel 1.
            for (int i = 0; i < P; ++i) {
                try {
                    t[i].join();
                    if(t[i].getFileNotFound() == true) {
                        System.out.println("File was not found by thread " + (i + 1));
                    }
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
            }
            
            br_order_input.close();
            br_order_output.close();
            br_product_output.close();
        } catch (IOException e) {
            //When file is not found.
            e.printStackTrace();
        }
    }
}
