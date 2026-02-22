package group.chon.agent.argo.jasonStdLib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import group.chon.agent.argo.Argo;

public class limit extends DefaultInternalAction {

    private static final long serialVersionUID = -4841692752581197132L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        final Argo argoArch = Argo.getArgoArch(ts.getAgArch());
        if (argoArch != null) {
            if (args[0].isNumeric()) {
                try{
                    int intLimit = (int) ((NumberTerm) args[0]).solve();
                    argoArch.setLimit(intLimit);
                    ts.getLogger().fine("Perception Filter (limit) was defined as: "+intLimit+" ms.");
                    return true;
                } catch (Exception e) {
                    ts.getLogger().severe("The Limit parameter must be Integer.");
                    return false;
                }
            } else {
                return false;
            }
        }else{
            ts.getLogger().warning("[WARNING] It was not possible to call internal action .act because this agent is not an Argo agent.");
            return false;
        }
    }
}