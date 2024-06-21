package group.chon.agent.argo.core.utils;

import group.chon.agent.argo.Argo;
import jason.JasonException;
import jason.architecture.AgArch;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ArgoUtils {

    private static final Logger LOGGER = Logger.getLogger("ARGO_UTILS");

    public static Argo checkArchClass(AgArch agArch, String internalActionName) throws JasonException {
        Argo argo = null;
        if(agArch instanceof Argo) {
            argo = (Argo) agArch;
        } else {
            throw new JasonException(
                    "Was not possible to call " + internalActionName + "internal action because this AgArch is not a Argo arch.");
        }
        return argo;
    }

    public static Argo checkArchClass(AgArch agArch) throws JasonException {
        Argo argo = null;
        if(agArch instanceof Argo) {
            argo = (Argo) agArch;
        } else {
            throw new JasonException(
                    "Was not possible to cast the agArch '" + agArch.getClass().getSimpleName() + "' to a Argo arch.");
        }
        return argo;
    }

    public static void log(Level level, String message) {
        try {
            LOGGER.log(level, message);
        } catch (Exception | Error e) {
            //ignore
        }
    }

}
