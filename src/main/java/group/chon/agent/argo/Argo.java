package group.chon.agent.argo;

import group.chon.javino.Javino;
import jason.RevisionFailedException;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import jason.architecture.AgArch;
import jason.asSyntax.PredicateIndicator;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;

public class Argo extends AgArch {

    public static final String DEFAULT_PORT = "COM1";

    private String argoVersion = "1.3-pre";

    public Javino javino = new Javino();

    private String port = "";

    long lastPerceived = 0;

    private long limit = 0;

    public Boolean blocked = true;

    Logger logger = Logger.getLogger("ARGO");

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
        logger.info("Using version "+argoVersion);
        super.init();
        this.setPort(DEFAULT_PORT);
    }

    @Override
    public Collection<Literal> perceive() {
        long perceiving = System.nanoTime();

        if (((perceiving - this.lastPerceived) < this.limit) || this.blocked) {
            return null;
        }
        this.lastPerceived = perceiving;

        int cont;
        List<Literal> jPercept = new ArrayList<Literal>();
        try {
            removeBeliefsBySource("proprioception");
            removeBeliefsBySource("interoception");
            removeBeliefsBySource("exteroception");
            if (this.javino.requestData(this.port, "getPercepts")) {
                String rwPercepts = this.javino.getData();

                if (rwPercepts.contains(";")) {
                    String[] perception = rwPercepts.split(";");
                    for (cont = 0; cont < perception.length; cont++) {
                        /* adopting source percepts from body*/
                        if (perception[cont].endsWith("[p]")) {
                            getTS().getAg().getBB().add(Literal.parseLiteral(perception[cont].replace("[p]","[source(proprioception)]")));
                        }else if (perception[cont].endsWith("[i]")) {
                            getTS().getAg().getBB().add(Literal.parseLiteral(perception[cont].replace("[i]","[source(interoception)]")));
                        }else if (perception[cont].endsWith("[e]")) {
                            getTS().getAg().getBB().add(Literal.parseLiteral(perception[cont].replace("[e]","[source(exteroception)]")));
                        }else{
                            jPercept.add(Literal.parseLiteral(perception[cont]));
                        }
                    }
                } else if (rwPercepts != null && !rwPercepts.isEmpty()) {
                    if(rwPercepts.endsWith("[p]")){
                        getTS().getAg().getBB().add(Literal.parseLiteral(rwPercepts.replace("[p]","[source(proprioception)]")));
                    }else if (rwPercepts.endsWith("[i]")){
                        getTS().getAg().getBB().add(Literal.parseLiteral(rwPercepts.replace("[i]","[source(interoception)]")));
                    }else if (rwPercepts.endsWith("[e]")){
                        getTS().getAg().getBB().add(Literal.parseLiteral(rwPercepts.replace("[e]","[source(exteroception)]")));
                    }else{
                        jPercept.add(Literal.parseLiteral(rwPercepts));
                    }
                } else {
                    this.getTS().getLogger().warning("[WARNING] There is no message coming from sensors.");
                }
            }
            return jPercept;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean canSleep() {
        return false;
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

    public void setLastPerceived(long lastPerceived) {
        this.lastPerceived = lastPerceived;
    }

    public long getLimit() {
        return this.limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public Boolean getBlocked() {
        return this.blocked;
    }

    public void setBlocked(Boolean blocked) {
        this.blocked = blocked;
    }

    public void removeBeliefsBySource(String source) throws RevisionFailedException {
        for (Literal belief : getTS().getAg().getBB()) {
            if (belief.hasAnnot()) {
                for (Term annotation : belief.getAnnots()) {
                    if (annotation.isStructure()) {
                        Structure annot = (Structure) annotation;
                        if (annot.getFunctor().equals("source") && annot.getTerm(0).equals(Literal.parseLiteral(source))) {
                            getTS().getAg().delBel(belief);
                        }
                    }
                }
            }
        }
    }
}
