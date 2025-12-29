/**
 * @author Xuanwei Zhang
 * @date 2025/12/026
 * @description: MAEI Algorithm
 * @Version: v1.0
 * */


import java.io.*;
import java.util.*;

public class AlgoHAEI {
    // algorithm parameters
    double maxMemory = 0;
    long startTimestamp = 0;
    long endTimestamp = 0;
    int haeiCount = 0;
    int joinCount = 0;
    double min_AE = 0.0;

    // data structure
    List<UtilityList> listOfUtilityLists = new ArrayList<UtilityList>();
    Map<Integer,Long> mapItemToINV;
    Map<Integer,Double> mapItemToAEUB;
    Map<Integer,Long> mapItemToMu;
    BufferedWriter writer = null;
    Map<Integer,Map<Integer,Double>> mapItemToEAES;


    // data structure for HAEI
    class Pair{
        int item = 0;
        int utility = 0;
        long rMinInv = 0;
    }

    public AlgoHAEI(){
    }

    // run the HAEI algorithm
    public void runAlgorithm(String input1, String intput2, String output, double min_AE) throws IOException{
        startTimestamp = System.currentTimeMillis();
        writer = new BufferedWriter(new FileWriter(output));
        mapItemToAEUB = new HashMap<Integer,Double>();
        mapItemToINV = new HashMap<Integer,Long>();
        mapItemToMu = new HashMap<Integer,Long>();
        mapItemToEAES = new HashMap<Integer,Map<Integer,Double>>();
        this.min_AE = min_AE;
        BufferedReader myInput = null;
        String thisLine;

        try{
            myInput = new BufferedReader(new InputStreamReader(new FileInputStream(new File(intput2))));
            while ((thisLine = myInput.readLine()) != null) {
                if (thisLine.isEmpty() == true ||
                        thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
                        || thisLine.charAt(0) == '@') {
                    continue;
                }
                String[] str = thisLine.split(" ");
                Integer item = Integer.valueOf(str[0]);
                Long invest = Long.parseLong(str[1]);
                mapItemToINV.put(item,invest);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(myInput != null){
                myInput.close();
            }
        }


        try {
            myInput = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input1))));
            while ((thisLine = myInput.readLine()) != null){
                if (thisLine.isEmpty() == true || thisLine.charAt(0) == '#') {
                    continue;
                }
                String[] split = thisLine.split(":");
                String[] items = split[0].split(" ");
                String[] utilityValues = split[2].split(" ");
                Integer transactionMUtility = Integer.MIN_VALUE;
                for (int i = 0; i < utilityValues.length; i++) {
                    if (transactionMUtility < Integer.parseInt(utilityValues[i])) {
                        transactionMUtility = Integer.parseInt(utilityValues[i]);
                    }
                }
                for(int i = 0; i < items.length; i++){
                    Integer item = Integer.parseInt(items[i]);
                    Long mu = mapItemToMu.get(item);
                    mu = (mu == null)?  transactionMUtility: mu + transactionMUtility;
                    mapItemToMu.put(item, mu);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(myInput != null){
                myInput.close();
            }
        }

        //  Calculate AEUB
        for (Integer item : mapItemToMu.keySet()) {
            Double aeub = mapItemToAEUB.get(item);
            double newaeub = (double)mapItemToMu.get(item) / mapItemToINV.get(item);
            aeub = (aeub == null)? newaeub : newaeub + aeub;
            mapItemToAEUB.put(item, aeub);
        }

        List<Integer> ItemLists = new ArrayList<Integer>();

        //AEUB Pruning Strategy
        for(Integer item : mapItemToAEUB.keySet()){
            if(mapItemToAEUB.get(item) >= min_AE){
                ItemLists.add(item);
            }
        }

        Collections.sort(ItemLists, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return mapItemToAEUB.get(o1).compareTo(mapItemToAEUB.get(o2));
            }
        });


        Map<Integer,UtilityList> mapItemToUtility = new HashMap<Integer,UtilityList>();

        for(Integer item : ItemLists){
            UtilityList utilityList = new UtilityList(item);
            utilityList.addInvestion(mapItemToINV.get(item));
            mapItemToUtility.put(item, utilityList);
        }

        try {
            myInput = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input1))));
            int tid = 0;
            while ((thisLine = myInput.readLine()) != null) {
                if (thisLine.isEmpty() == true || thisLine.charAt(0) == '#') {
                    continue;
                }
                String[] split = thisLine.split(":");
                String[] items = split[0].split(" ");
                String[] utilityValues = split[2].split(" ");


                double maxUtilityOfCurrentTranscation = 0;

                //  revised transaction
                List<Pair> revisedTransaction = new ArrayList<Pair>();
                for (int i = 0; i < items.length; i++){
                    Pair pair = new Pair();
                    pair.item = Integer.parseInt(items[i]);
                    pair.utility = Integer.parseInt(utilityValues[i]);
                    pair.rMinInv = mapItemToINV.get(pair.item);
                    if(mapItemToAEUB.get(pair.item) >= min_AE){
                        revisedTransaction.add(pair);
                        if(maxUtilityOfCurrentTranscation < pair.utility){
                            maxUtilityOfCurrentTranscation = pair.utility;
                        }
                    }
                }

                Collections.sort(revisedTransaction, new Comparator<Pair>() {
                    @Override
                    public int compare(Pair o1, Pair o2) {
                        return mapItemToAEUB.get(o1.item).compareTo(mapItemToAEUB.get(o2.item));
                    }
                });

                // Construct the initial AE-List
                for(int i = 0;i < revisedTransaction.size(); i++){
                    Pair pair = revisedTransaction.get(i);
                    int rmu = 0;
                    long rmi = pair.rMinInv;
                    for(int j = i+1; j < revisedTransaction.size(); j++){
                        Pair pair1 = revisedTransaction.get(j);
                        if(pair1.utility > rmu) {
                            rmu = pair1.utility;
                        }
                        if(pair1.rMinInv < rmi) {
                            rmi = pair1.rMinInv;
                        }
                    }

                    int mn = revisedTransaction.size() - 1 - i;
                    UtilityList utilityListOfItem = mapItemToUtility.get(pair.item);
                    Element element = new Element(tid, pair.utility, rmu, rmi,mn);
                    utilityListOfItem.addElement(element,mn);
                    Map<Integer,Double> mapEAESItem = mapItemToEAES.get(pair.item);

                    // EAECS
                    if(mapEAESItem == null){
                        mapEAESItem = new HashMap<Integer,Double>();
                        mapItemToEAES.put(pair.item,mapEAESItem);
                    }
                    for(int j = i+1;j < revisedTransaction.size();j++){
                        Pair pairAfter = revisedTransaction.get(j);
                        Double invOfItem =(double)(mapItemToINV.get(pair.item) + mapItemToINV.get(pairAfter.item));
                        Double aeubSum = mapEAESItem.get(pairAfter.item);
                        if(aeubSum == null) {
                            mapEAESItem.put(pairAfter.item, maxUtilityOfCurrentTranscation / invOfItem);
                        }else {
                            mapEAESItem.put(pairAfter.item,aeubSum + maxUtilityOfCurrentTranscation / invOfItem);
                        }
                    }
                }
                tid++;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(myInput != null){
                myInput.close();
            }
        }

        listOfUtilityLists.addAll(mapItemToUtility.values());

        Collections.sort(listOfUtilityLists, new Comparator<UtilityList>() {
            @Override
            public int compare(UtilityList o1, UtilityList o2) {
                return mapItemToAEUB.get(o1.item).compareTo(mapItemToAEUB.get(o2.item));
            }
        });

        checkMemory();
        HAEIMine(new int[0],null,listOfUtilityLists, min_AE);
        checkMemory();
        writer.close();
        endTimestamp = System.currentTimeMillis();
    }


    // Mining Algorithm
    private void HAEIMine(int[] prefix,UtilityList pUL,List<UtilityList> ULs,double min_AE) throws IOException {
        for (int i = 0; i < ULs.size(); i++){
            UtilityList X = ULs.get(i);
            int length = prefix.length +1;
            if (X.Sumae /length >= min_AE){
                writeOut(prefix, X.item ,X.Sumae /length);
            }

            // Calculate the MAE of X
            double Mae;
            if(X.mn == 0) {
                Mae = 0;
            } else{
                Mae = calMae(X, length);
            }

            // MAE Pruning Strategy
            if (Mae >= min_AE){
                List<UtilityList> exULs = new ArrayList<UtilityList>();
                for (int j = i+1; j < ULs.size(); j++){
                    UtilityList Y = ULs.get(j);
                    //EAES-Prune
                    Map<Integer,Double> mapEAES = mapItemToEAES.get(X.item);
                    if(mapEAES != null) {
                        Double aeubY = mapEAES.get(Y.item);
                        if (aeubY == null || aeubY < min_AE) {
                            continue;
                        }
                    }
                    exULs.add(construct(pUL,X,Y));
                }

                int [] newPrefix = new int[prefix.length+1];
                System.arraycopy(prefix, 0, newPrefix, 0, prefix.length);
                newPrefix[prefix.length] = X.item;
                // Recursive Mining
                HAEIMine(newPrefix, X, exULs, min_AE);
            }
        }
    }

    /**
     * Calculate the MAE of a utility list
     */
    private double calMae(UtilityList ul, int length){
        double sumMau = 0;

        for(int i = 0;i<ul.elements.size();i++){
            Element element = ul.elements.get(i);
            double au = (double) element.iutils/length ;
            double tmae;
            double invNew = (double)(ul.inv + element.rMinInv);
            if (element.rmu != 0) {
                if(element.rmu > au) {
                    tmae = (double) (element.iutils  + element.rmu * element.nofitem) / ((length + element.nofitem)*invNew);
                }else {
                    tmae = (double) (element.iutils  + element.rmu) /((length + 1)*invNew);
                }
            }
            else {
                tmae = 0;
            }
            sumMau = sumMau + tmae;
        }
        return sumMau;
    }


    /**
     * Construct a utility list that contains all elements of X and Y
     * @param P
     * @param px
     * @param py
     * @return the utility list of Pxy
     */
    private UtilityList construct(UtilityList P,UtilityList px,UtilityList py){
        UtilityList pxyUL = new UtilityList(py.item);
        long invOfItem = 0;
        if(P == null) {
            invOfItem = px.getInv() + py.getInv();
        }else{
            invOfItem = px.getInv() + py.getInv() - P.getInv();
        }
        pxyUL.addInvestion(invOfItem);

        for(Element ex : px.elements){
            Element ey =  findElementWithTID(py, ex.tid);
            if (ey == null){
                continue;
            }
            if (P == null){
                Element eXY = new Element(ex.tid, ex.iutils + ey.iutils, ey.rmu, ey.rMinInv,ey.nofitem);
                pxyUL.addElement(eXY,py.getMn());
            }else{
                Element e = findElementWithTID(P, ex.tid);
                if (e != null) {
                    Element eXY = new Element(ex.tid, ex.iutils + ey.iutils - e.iutils, ey.rmu, ey.rMinInv,ey.nofitem);
                    pxyUL.addElement(eXY, py.getMn());
                }
            }
        }

        joinCount++;
        return pxyUL;
    }



    /**
     * This method is to find an element in a utility list with a given tid
     * @param ulist the utility list
     * @param tid the tid
     * @return the element
     */
    private Element findElementWithTID(UtilityList ulist, int tid){
        List<Element> list = ulist.elements;
        int first = 0;
        int last = list.size() - 1;
        while( first <= last ) {
            int middle = ( first + last ) >>> 1;
            if(list.get(middle).tid < tid){
                first = middle + 1;
            }
            else if(list.get(middle).tid > tid){
                last = middle - 1;
            }
            else{
                return list.get(middle);
            }
        }
        return null;
    }


    // write to output file
    private void writeOut(int[] prefix, int item, double ae) throws IOException {
        haeiCount++;
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < prefix.length; i++) {
            buffer.append(prefix[i]);
            buffer.append(' ');
        }
        buffer.append(item);
        buffer.append("#AE: ");
        buffer.append(ae);
        writer.write(buffer.toString());
        writer.newLine();
    }



    // Check the memory usage and keep the maximum memory usage
    private void checkMemory() {
        double currentMemory = (Runtime.getRuntime().totalMemory() -  Runtime.getRuntime().freeMemory())/1024d/1024d;
        if (currentMemory > maxMemory) {
            maxMemory = currentMemory;
        }
    }


    // Print statistics
    public void printStats() {
        System.out.println("=============  HAEI-MINER ALGORITHM =============");
        System.out.println(" The final minae : " + min_AE);
        System.out.println(" High average-efficiency itemsets count : " + haeiCount);
        System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
        System.out.println(" Memory ~ " + maxMemory + " MB");
        System.out.println(" Join count : " + joinCount);
        System.out.println("===================================================");
    }
}
