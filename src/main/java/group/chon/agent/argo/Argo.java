package group.chon.agent.argo;

import group.chon.javino.Javino;
import jason.asSyntax.Literal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import jason.architecture.AgArch;

public class Argo extends AgArch {

    private boolean CAN_SLEEP = false;
    private int MIN_TIME_NAP = 50;
    private int MAX_TIME_NAP = 250;

    private final String VERSION = "1.2.6";

    public static final String DEFAULT_PORT = "COM1";

    private Javino javino = new Javino();

    private String port = "";

    private long lastPerceived = 0;
    private long lastWakeUp = 0;

    private int limit = 0;

    public Boolean blocked = true;

    public static Argo getArgoArch(AgArch currentArch) {
        if (currentArch == null) {
            return null;
        }
        if (currentArch instanceof Argo) {
            return (Argo) currentArch;
        }
        return getArgoArch(currentArch.getNextAgArch());
    }

    @Override
    public void init() throws Exception {
        super.init();
        this.javino.infoPortStatus(true);
        this.javino.timeout(2000);
        this.setPort(DEFAULT_PORT);
        this.getTS().getLogger().info("ARGO Agent Architecture ["+this.VERSION+"]");
    }

    @Override
    public Collection<Literal> perceive() {
        long perceiving = System.currentTimeMillis();
        if (((perceiving - getLastPerceived()) < getLimit()) || isBlocked()) {return null;}

        this.getTS().getLogger().fine("[ARGO] Perceiving ...");
        int cont;
        List<Literal> jPercept = new ArrayList<Literal>();
        try {
            if (this.javino.requestData(this.port, "getPercepts")) {
                String rwPercepts = this.javino.getData();
                if (rwPercepts.contains(";")) {
                    String[] perception = rwPercepts.split(";");
                    for (cont = 0; cont < perception.length; cont++) {
                        jPercept.add(Literal.parseLiteral(perception[cont]));
                    }
                } else if (rwPercepts != null && !rwPercepts.isEmpty()) {
                    jPercept.add(Literal.parseLiteral(rwPercepts));
                } else {
                    this.getTS().getLogger().warning("[WARNING] There is no message coming from sensors.");
                }
                this.getTS().getLogger().fine("\t"+rwPercepts);
            }else{
                this.getTS().getLogger().severe("ERROR this.javino.requestData == false");
            }
            this.setLastPerceived();
        } catch (Exception e) {
            this.setLastPerceived();
            return null;
        }
        this.getTS().getLogger().fine("[ARGO] Perceived!");
        return jPercept;
    }

    @Override
    public boolean canSleep() {
        boolean isCan;
        if (this.CAN_SLEEP) {                                                // defined by .argo.port(P,true); --> canSleep(true)
            isCan = true;
        } else{
            long elapsed = System.currentTimeMillis() - getLastWakeUp();    // milliseconds after last canSleep(false)
            if (elapsed < MIN_TIME_NAP ){                                   // has not slept for MIN_TIME_NAP yet
                isCan = true;
            }else{
                if(getLimit() <= 0){                                        // if was not defined a .argo.limit(M)
                    isCan = elapsed < MAX_TIME_NAP;                         // can sleep until MAX_TIME_NAP
                }else{                                                      // if was defined a .argo.limit(M)
                    isCan = elapsed < Math.min(getLimit(), MAX_TIME_NAP);   // can sleep only until M, if M < MAX_TIME_SLEEPING
                }
            }
        }
        if(!isCan){setLastWakeUp();}                                        // if agent needs wake-up, save the time
        //this.getTS().getLogger().fine("[ARGO] canSleep("+isCan+")");
        return isCan;
    }

    public void setCAN_SLEEP(boolean sleep){
        this.CAN_SLEEP = sleep;
    }

    public Javino getJavino() {
        return this.javino;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        if(port.equals("none") || port.equals("/dev/none") ){
            setBlocked(true);
            javino.closePort();
        }
        this.port = port;
    }

    public long getLastPerceived() {
        return this.lastPerceived;
    }

    public void setLastPerceived() {
        this.lastPerceived = System.currentTimeMillis();
    }

    public long getLastWakeUp() {
        return this.lastWakeUp;
    }

    public void setLastWakeUp() {
        this.lastWakeUp = System.currentTimeMillis();
    }

    public long getLimit() {
        return this.limit;
    }

    public void setLimit(int limit) {
        this.javino.timeout(2*limit);
        this.limit = limit;
    }

    public Boolean isBlocked() {
        return this.blocked;
    }

    public void setBlocked(Boolean blocked) {
        this.blocked = blocked;
    }

}
