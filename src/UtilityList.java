/**
 * @author Xuanwei Zhang
 * @date 2025/12/026
 * @description: MAEI Algorithm
 * */
import java.util.ArrayList;
import java.util.List;

public class UtilityList {
    int item;
    int mn = 0;
    long inv = 0;
    double Sumae = 0;


    List<Element> elements = new ArrayList<Element>();

    // Constructor
    public UtilityList(int item) {this.item = item;}

    // add an investment to the utility list of a transaction
    public void addInvestion(long invOfItem){
        inv = invOfItem;
    }

   // add an element to the utility list of a transaction
    public void addElement(Element element, int n) {
        Sumae += (double) element.iutils / inv;
        elements.add(element);
        if(mn < n) {
            mn = n;
        }
    }

    // get the minimum number of items
    public int getMn(){return mn;}


    // get the sum of all utilities
    public long getInv(){
        return inv;
    }
}
