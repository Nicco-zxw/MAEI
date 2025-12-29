/**
 * @author Xuanwei Zhang
 * @date 2025/12/026
 * @description: MAEI Algorithm
 * */
class Element {
    // TID of the transaction
    final int tid;
    // item utilities
    final int iutils;
    // maximum remaining utility value of the transaction
    final int rmu;
    // minimum remaining investment value of the transaction
    final long rMinInv;
    // number of remaining items
    final int nofitem;

    public Element(int tid, int iutils, int rmu,long rMinInv,int nofitem){
        this.tid = tid;
        this.iutils = iutils;
        this.rmu = rmu;
        this.rMinInv = rMinInv;
        this.nofitem = nofitem;
    }
}
