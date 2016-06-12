package pl.edu.pjwstk.kaldi.grammars;

import java.util.LinkedList;

/**
 * Created by guest on 6/1/16.
 */
public class Akt {

    public static Grammar male_names() {
        Grammar ret=new Grammar();
        LinkedList<String> l=new LinkedList<>();
        l.add("stanisław");
        l.add("aleksander");
        l.add("jan");
        l.add("michał");
        ret.setWordList(l);
        return ret;
    }

    public static Grammar female_names() {
        Grammar ret=new Grammar();
        LinkedList<String> l=new LinkedList<>();
        l.add("teofila");
        l.add("marianna");
        l.add("jadwiga");
        l.add("natalia");
        l.add("aleksandra");
        ret.setWordList(l);
        return ret;
    }

    public static Grammar last_names() {
        Grammar ret=new Grammar();
        LinkedList<String> l=new LinkedList<>();
        l.add("doddek");
        l.add("dąbkowska");
        l.add("dudziak");
        l.add("ciborowska");
        l.add("kowalski");
        ret.setWordList(l);
        return ret;
    }

    public static Grammar places() {
        Grammar ret=new Grammar();
        LinkedList<String> l=new LinkedList<>();
        l.add("lipianka");
        l.add("borawe");
        l.add("warszawa");
        l.add("poznań");
        l.add("kraków");
        ret.setWordList(l);
        return ret;
    }

    public static Grammar zgonu() {

        Grammar w_akt=new Grammar();
        w_akt.setWord("akt");

        Grammar num=Numbers.numbers();
        num.fixend();

        w_akt.attach(num);

        Grammar w_pl=new Grammar();
        w_pl.setWord("miejscowość");
        w_akt.attach(w_pl);

        w_akt.attach(places());

        Grammar w_zm=new Grammar();
        w_zm.setWord("zmarła");
        w_akt.attach(w_zm);

        w_akt.attach(female_names());
        w_akt.attach(last_names());

        Grammar w_fat=new Grammar();
        w_fat.setWord("ojciec");
        w_akt.attach(w_fat);

        w_akt.attach(male_names());
        w_akt.attach(last_names());

        Grammar w_mot=new Grammar();
        w_mot.setWord("matka");
        w_akt.attach(w_mot);

        w_akt.attach(female_names());
        w_akt.attach(last_names());

        return w_akt;
    }

}
