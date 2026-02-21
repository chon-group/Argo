package group.chon.agent.argo;

import group.chon.javino.Javino;
import jason.asSyntax.Literal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import jason.architecture.AgArch;

public class Argo extends AgArch {

    private Logger logger;
    public static final String DEFAULT_PORT = "COM1";

    private Javino javino = new Javino();

    private String port = "";

    private long lastPerceived = 0;

    private int limit = 250;

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
        this.logger = Logger.getLogger(getAgName());
        this.javino.infoPortStatus(true);
        this.javino.timeout(2000);
        this.setPort(DEFAULT_PORT);
    }

    @Override
    public Collection<Literal> perceive() {
        long perceiving = System.nanoTime();
        if (((perceiving - getLastPerceived()) < getLimit()) || isBlocked()) {return null;}

        logger.fine("[ARGO] Perceiving...");
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
            }
            this.setLastPerceived();
        } catch (Exception e) {
            this.setLastPerceived();
            return null;
        }
        logger.fine("[ARGO] Perceived!");
        return jPercept;
    }

    /*@Override
    public boolean canSleep() {
        return false;
    }*/

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
        this.lastPerceived = System.nanoTime();
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
