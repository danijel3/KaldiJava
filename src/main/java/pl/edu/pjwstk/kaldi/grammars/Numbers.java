package pl.edu.pjwstk.kaldi.grammars;

import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

public class Numbers {


    public static Grammar numbers() {

        Grammar zero = new Grammar();
        zero.setWord("zero");

        Map<Integer, Integer> join_link = new TreeMap<>();
        join_link.put(0, 0);
        join_link.put(1, 1);

        Grammar n1_9 = new Grammar();
        LinkedList<String> l1_9 = new LinkedList<>();
        l1_9.add("jeden");
        l1_9.add("dwa");
        l1_9.add("trzy");
        l1_9.add("cztery");
        l1_9.add("pięć");
        l1_9.add("sześć");
        l1_9.add("siedem");
        l1_9.add("osiem");
        l1_9.add("dziewięć");
        n1_9.setWordList(l1_9);

        Grammar n1_99 = n1_9.clone();

        Grammar n10_19 = new Grammar();
        LinkedList<String> l10_19 = new LinkedList<>();
        l10_19.add("dziesięć");
        l10_19.add("jedenaście");
        l10_19.add("dwanaście");
        l10_19.add("trzynaście");
        l10_19.add("czternaście");
        l10_19.add("piętnaście");
        l10_19.add("szesnaście");
        l10_19.add("siedemnaście");
        l10_19.add("osiemnaście");
        l10_19.add("dziewiętnaście");
        n10_19.setWordList(l10_19);

        n1_99.merge(n10_19, join_link);

        Grammar n20_90 = new Grammar();
        LinkedList<String> l20_90 = new LinkedList<>();
        l20_90.add("dwadzieścia");
        l20_90.add("trzydzieści");
        l20_90.add("czterdzieści");
        l20_90.add("piećdziesiąt");
        l20_90.add("sześcdziesiąt");
        l20_90.add("siedemdziesiąt");
        l20_90.add("osiemdziesiąt");
        l20_90.add("dziewiedziesiąt");
        n20_90.setWordList(l20_90);

        Grammar n2x_9x = n20_90.clone();
        n2x_9x.attach(n1_9, 1);
        n2x_9x.end_nodes.add(1);


        Map<Integer, Integer> join_link2 = new TreeMap<>();
        join_link2.put(0, 0);
        join_link2.put(2, 1);

        n1_99.merge(n2x_9x, join_link2);

        Grammar n100_900 = new Grammar();
        LinkedList<String> l100_900 = new LinkedList<>();
        l100_900.add("<eps>");
        l100_900.add("sto");
        l100_900.add("dwieście");
        l100_900.add("trzysta");
        l100_900.add("czterysta");
        l100_900.add("pięćset");
        l100_900.add("sześćset");
        l100_900.add("siedemset");
        l100_900.add("osiemset");
        l100_900.add("dziewięćset");
        n100_900.setWordList(l100_900);

        Grammar n1xx_9xx = n100_900.clone();
        n1xx_9xx.attach(n1_99, 1);
        n1xx_9xx.end_nodes.add(1);

        Grammar n0_999 = zero.clone();
        n0_999.merge(n1xx_9xx, join_link2);

        return n0_999;
    }
}
