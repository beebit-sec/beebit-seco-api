package tw.edu.au.csie.ucan.beebit.seco;

public class Project {
    private String pid;
    private String uid;

    public Project(String uid, String pid) {
        this.uid = uid;
        this.pid = pid;
    }

    public String getUid() { return uid;}
    public String getPid() { return pid;}
}
