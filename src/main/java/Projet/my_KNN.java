/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Projet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;

/**
 *
 * @author aurel
 */
public class my_KNN {
    private int nb_line = 46;
    private double [][] dataset = new double[nb_line][200];
    private String [] categories = new String [nb_line];

    public my_KNN() {
    }
    
    /////////// Fonctions utilitaires //////////
    //Calcul de la distance
    private double dist (double [] A, double [] B){
        double res = 0;
        for (int i=0; i<A.length;i++){
            res += Math.pow(A[i] - B[i], 2);
        }
        return Math.sqrt(res);
    }
    
    //Retourne indice du min
    private int ArgMin(double [] A){
        int res =0;
        for (int i=1; i<A.length;i++){
            if (A[res]>A[i]){
                res = i;
            }
        }
        return res;
    }
    
    //Retourne l'indice du max
    private int ArgMax(int [] A){
        int res =0;
        for (int i=1; i<A.length;i++){
            if (A[res]<A[i]){
                res = i;
            }
        }
        return res;
    }
    
    //Retourne les k indices min
    public int[] KArgMin(double [] A, int k){
        int[] res = new int [k];
        for (int i=0; i<k;i++){
            res[i] = ArgMin(A);
            A[res[i]] = 100000;
        }
        return res;
    }
    
    ////////// Fonction principale /////////
    //Evaluation avec uniquement le plus proche voisin
    public int eval(double [] resultat)
    {
        double [] Distance = new double [this.nb_line];
        for (int i=0; i<this.nb_line;i++){
            Distance[i] = dist(dataset[i], resultat);
        }
        
        int res = 2;
        int a = ArgMin(Distance);
        
        String cat = categories[a];
        if (cat.equals("I")){
            res = 0;
        }else if (cat.equals("R")){
            res = 1;
        }else if (cat.equals("N")){
            res = 2;
        }
        
        if (Distance[a]>5){
            res = 2;
        }
        return res;
    }
    
    //Evaluation avec les k plus proches voisins
    public int eval(double [] resultat, int k)
    {
        double [] Distance = new double [this.nb_line];
        for (int i=0; i<this.nb_line;i++){
            Distance[i] = dist(dataset[i], resultat);
        }
        
        int res = 3;
        int[] a = KArgMin(Distance.clone(), k);
        
        int cat[] = {0, 0, 0};
        for (int i=0;i<k;i++){
            if (categories[a[i]].equals("I")){
                cat[0]++;
            }else if (categories[a[i]].equals("R")){
                cat[1]++;
            }else if (categories[a[i]].equals("N")){
                cat[2]++;
            }
        }
        
        System.out.print("cat: ");
        System.out.print(cat[0]);
        System.out.print(" ");
        System.out.print(cat[1]);
        System.out.print(" ");
        System.out.println(cat[2]);
        
        if (Distance[a[0]] >5)
            return 2;
        else{
            return ArgMax(cat);
        }
    }
    
     //Restoration d'un entrainement
    public void file_reader() {
        String line;
        
        int index_line = 0;
        
        try {
            File myObj = new File("ads2.txt");
            Scanner myReader = new Scanner(myObj);
            
            while (myReader.hasNextLine()) {
                line = myReader.nextLine();
                
                if (!(line.equals(""))){
                    if (!(line.substring(0, 1).equals("@"))){
                        String [] line_tab = line.split(",");
                        for (int i=0;i<200;i++){
                            dataset[index_line][i] = Double.parseDouble(line_tab[i]);
                        }
                        categories[index_line] = line_tab[200];
                        index_line++;
                    }
                }
            }
            myReader.close();
            
            
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
    
    //Nouvel entrainement
    public void train(){
        String line;
        int index_line = 0;
        String [] classes = new String [this.nb_line];
        String [] phrases = new String [this.nb_line];
        
        //Recuperation du dataset
        try {
            File myObj = new File("dataset.txt");
            Scanner myReader = new Scanner(myObj);
            
            while (myReader.hasNextLine()) {
                line = myReader.nextLine();
                
                String [] line_tab = line.split(";");
                classes[index_line] = line_tab[0];
                phrases[index_line] = line_tab[1];
                
                index_line++;
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        
        //Init word2vec
        my_word2vec transfo = new my_word2vec();
        transfo.file_reader();
        double [] res;

        //Ecriture du fichier + application du word2vec a tout le dataset
        try {
            FileWriter myWriter = new FileWriter("ads2.txt");
            myWriter.write("@relation ads\n");
            myWriter.write("\n");

            for (int i=0;i<200; i++)
            {
                myWriter.write("@attribute A" + i + " numeric\n");
            }
            myWriter.write("@attribute profit {I, R, N}\n");
            myWriter.write("\n");
            myWriter.write("@data\n");

            for (int i=0;i<phrases.length; i++)
            {
                System.out.println(phrases[i]);
                res = transfo.word2vec(phrases[i]);
                for (int j=0; j<res.length; j++)
                {
                    myWriter.write(res[j] + ",");
                }
                myWriter.write(classes[i] + "\n");

            }

            myWriter.close();

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
    
    //Exemples
    public static void main(String[] args) {
        my_KNN a = new my_KNN();
        //a.train();
        a.file_reader();
        int k = 5;
        
        my_word2vec transfo = new my_word2vec();
        transfo.file_reader();
        double [] res = transfo.word2vec("je souhaite reserver");
        System.out.println(a.eval(res, k));
        res = transfo.word2vec("puis je reserver");
        System.out.println(a.eval(res, k));
        res = transfo.word2vec("puis je avoir une reservation");
        System.out.println(a.eval(res, k));
        res = transfo.word2vec("je souhaite recevoire une info");
        System.out.println(a.eval(res, k));
        res = transfo.word2vec("creneau libre");
        System.out.println(a.eval(res, k));
        res = transfo.word2vec("y a-t-il encore des creneaux de disponible");
        System.out.println(a.eval(res, k));
        res = transfo.word2vec("j'ai faim");
        System.out.println(a.eval(res, k));
        res = transfo.word2vec("J'adore le big data");
        System.out.println(a.eval(res, k));
    }
}

//0:Info, 1:Reservation, 2:Neutre
