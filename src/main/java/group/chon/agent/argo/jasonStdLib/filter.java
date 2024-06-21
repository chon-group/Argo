package group.chon.agent.argo.jasonStdLib;

import group.chon.agent.argo.Argo;
import group.chon.agent.argo.core.FilterActionEnum;
import group.chon.agent.argo.core.utils.ArgoUtils;
import group.chon.agent.argo.core.utils.ArgsUtils;
import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

import java.util.logging.Level;

public class filter extends DefaultInternalAction {

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 3;
    }

    @Override
    protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args);

        String filterActionName = ArgsUtils.getInString(args[0]);
        FilterActionEnum filterAction = FilterActionEnum.getFilterAction(filterActionName);
        if (filterAction == null) {
            String msgError = "Error: The filter action \""+filterActionName+"\" does not exists!";
            ArgsUtils.log(Level.SEVERE, msgError);
            throw JasonException.createWrongArgument(this, msgError);
        }

    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws JasonException {
        this.checkArguments(args);

        Argo argo = ArgoUtils.checkArchClass(ts.getAgArch(), this.getClass().getName());
        argo.addFilter(args);

        return true;
    }
}
