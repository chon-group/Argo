package group.chon.agent.argo.core.utils;

import jason.JasonException;
import jason.asSemantics.InternalAction;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Term;
import jason.asSyntax.parser.ParseException;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ArgsUtils {

    private static final Logger LOGGER = Logger.getLogger("ARGO_ARGS_UTILS");

    public static String getInString(Term arg) {
        return arg.toString().trim().replace("\"", "");
    }

    public static ListTerm getInListTerm(Term arg, InternalAction internalAction) throws JasonException {
        ListTerm listTerm;
        try {
            listTerm = ASSyntax.parseList(arg.toString());
        } catch (ParseException e) {
            String msgError = "Error: When converting the list received by argument ('" + arg.toString()
                    + "') to the internal action ('" + internalAction.getClass().getName() + "').";
            ArgsUtils.log(Level.SEVERE, msgError);
            throw JasonException.createWrongArgument(internalAction, msgError);
        }
        return listTerm;
    }

    public static ListTerm getInListTerm(Term term) throws JasonException {
        ListTerm listTerm;
        try {
            listTerm = ASSyntax.parseList(term.toString());
        } catch (ParseException e) {
            String msgError = "Error: When converting the list ('" + term.toString() + "').";
            ArgsUtils.log(Level.SEVERE, msgError);
            throw new JasonException(msgError, JasonException.WRONG_ARGS);
        }
        return listTerm;
    }

    public static void log(Level level, String message) {
        try {
            LOGGER.log(level, message);
        } catch (Exception | Error e) {
            //ignore
        }
    }

}
