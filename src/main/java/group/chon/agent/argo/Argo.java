package group.chon.agent.argo;

import group.chon.agent.argo.core.FilterActionEnum;
import group.chon.agent.argo.core.FilterActionProcessor;
import group.chon.agent.argo.core.utils.ArgsUtils;
import group.chon.javino.Javino;
import jason.architecture.AgArch;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Argo extends AgArch {

    public static final String DEFAULT_PORT = "COM1";

    public Javino javino = new Javino();

    private String port = "";

    long lastPerceived = 0;

    private long limit = 0;

    public Boolean blocked = true;

    private List<Term[]> filterConfigurations = new ArrayList<>();

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
            if (this.javino.requestData(this.port, "getPercepts")) {
                String rwPercepts = this.javino.getData();
                if (rwPercepts != null && !rwPercepts.isEmpty()) {
                    if (rwPercepts.contains(";")) {
                        String[] perceptions = rwPercepts.split(";");
                        if (!this.filterConfigurations.isEmpty()) {
                            perceptions = FilterActionProcessor.doFilters(perceptions, this.filterConfigurations);
                        }
                        for (cont = 0; cont < perceptions.length; cont++) {
                            jPercept.add(Literal.parseLiteral(perceptions[cont]));
                        }
                    } else {
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

    public void addFilter(Term[] filterConfiguration) {
        String filterActionName = ArgsUtils.getInString(filterConfiguration[0]);
        FilterActionEnum filterAction = FilterActionEnum.getFilterAction(filterActionName);
        if (FilterActionEnum.REMOVE.equals(filterAction)) {
            this.filterConfigurations = new ArrayList<>();
        } else {
            this.filterConfigurations.add(filterConfiguration);
        }
    }

}
