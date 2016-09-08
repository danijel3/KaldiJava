package pl.edu.pjwstk.kaldi.files.julius;

import java.util.Vector;

public class LatticeNode {
    public int id;
    public int time_start, time_end;
    public Vector<Integer> left, right;
    public Vector<Double> left_score, right_score;
    public double lscore, f, f_prev, g_head, g_prev;
    public double forward_score, backword_score, AMavg, cmscore, graphcm;
    public String headphone, tailphone;
    public String word;

    public Object object;

    /***********
     * Example:
     *
     * 19: [414..448] left=15,10 right=22 left_lscore=-39.554115,-51.541363
     * right_lscore=-57.233547 lscore_tmp=-57.233547 wid=4411 name="dobra" lname="dobra"
     * f=-85518.468750 f_prev=-85517.171875 g_head=-66810.640625 g_prev=-65217.273438
     * forward_score=-3157.783691 backword_score=-916.954895 AMavg=-45.524776 cmscore=0.079091
     * graphcm=0.003168 headphone=m-d+o tailphone=r-a+tS
     **********/
    public LatticeNode(String line) throws NumberFormatException, IndexOutOfBoundsException {
        left = new Vector<>();
        right = new Vector<>();
        left_score = new Vector<>();
        right_score = new Vector<>();

        String[] arr = line.split("\\s+");
        String[] arr2;

        if (arr.length < 2)
            throw new RuntimeException("Error parsing line [not enough tokens]: " + line);

        id = Integer.parseInt(arr[0].substring(0, arr[0].length() - 1));

        arr2 = arr[1].split("\\.\\.");
        time_start = Integer.parseInt(arr2[0].substring(1));
        time_end = Integer.parseInt(arr2[1].substring(0, arr2[1].length() - 1));

        String key, value;
        for (int el = 2; el < arr.length; el++) {
            arr2 = arr[el].split("=");
            key = arr2[0];
            value = arr2[1];

            switch (key) {
                case "left":
                    arr2 = value.split(",");
                    for (String s : arr2) {
                        left.add(Integer.parseInt(s));
                    }
                    break;
                case "right":
                    arr2 = value.split(",");
                    for (String s : arr2) {
                        right.add(Integer.parseInt(s));
                    }
                    break;
                case "left_lscore":
                    arr2 = value.split(",");
                    for (String s : arr2) {
                        left_score.add(Double.parseDouble(s));
                    }
                    break;
                case "right_lscore":
                    arr2 = value.split(",");
                    for (String s : arr2) {
                        right_score.add(Double.parseDouble(s));
                    }
                    break;
                case "lscore_tmp":
                    lscore = Double.parseDouble(value);
                    break;
                case "name":
                    word = value.replace("\"", "");
                    break;
                case "f":
                    f = Double.parseDouble(value);
                    break;
                case "f_prev":
                    f_prev = Double.parseDouble(value);
                    break;
                case "g_head":
                    g_head = Double.parseDouble(value);
                    break;
                case "g_prev":
                    g_prev = Double.parseDouble(value);
                    break;
                case "forward_score":
                    forward_score = Double.parseDouble(value);
                    break;
                case "backword_score":
                    backword_score = Double.parseDouble(value);
                    break;
                case "AMavg":
                    AMavg = Double.parseDouble(value);
                    break;
                case "cmscore":
                    cmscore = Double.parseDouble(value);
                    break;
                case "graphcm":
                    graphcm = Double.parseDouble(value);
                    break;
                case "headphone":
                    headphone = value;
                    break;
                case "tailphone":
                    tailphone = value;
                    break;
                default:
                    break;
            }
        }
    }


}
