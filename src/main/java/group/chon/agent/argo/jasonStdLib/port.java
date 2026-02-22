package group.chon.agent.argo.jasonStdLib;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Term;
import group.chon.agent.argo.Argo;

public class port extends DefaultInternalAction {

    private static final long serialVersionUID = -4841692752581197132L;
    private String portAddress;
    private Boolean canSleep;

    @Override
    public int getMinArgs() { return 1; }

    @Override
    public int getMaxArgs() { return 2; }

    @Override
    protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args);
        this.portAddress = null;
        this.canSleep = null;

        /* EXPECTED -->
            .argo.port(P); -- P is a Literal or String representing the port identification(e.g., ttyUSB0, "COM8", "ttyUSB0").
        */
        if (args.length == 1 && (args[0].isAtom() || args[0].isString())){
            if(args[0].isAtom()){this.portAddress = args[0].toString();}
            else{this.portAddress = unquote(args[0].toString());}

            this.canSleep = false;      /* default */

            return;
        }

        /* EXPECTED -->
            .argo.port(P,S);
                -- P is a Literal or String representing the port identification(e.g., ttyUSB0, "COM8", "ttyUSB0").
                -- S is a boolean representing if the agent can sleep (true|false).
        */
        if (args.length == 2 && (args[0].isAtom() || args[0].isString()) && args[1].isAtom() ) {
            if(args[0].isAtom()){this.portAddress = args[0].toString();}
            else{this.portAddress = unquote(args[0].toString());}

            if (atomToBoolean((Atom) args[1])) {this.canSleep = true;}
            else if (!atomToBoolean((Atom) args[1])) {this.canSleep = false;}

            return;
        }

        throw JasonException.createWrongArgument(
                this,
                "Usage: .argo.port(P) or .argo.port(P, true|false)  (P atom or string) \n" +
                        "\t Consult https://github.com/chon-group/Argo/wiki/"
        );
    }


    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);
        final Argo argoArch = Argo.getArgoArch(ts.getAgArch());
        if (argoArch != null) {
            String os = System.getProperty("os.name");
            if (os.substring(0, 1).equals("W")) {
                argoArch.setCAN_SLEEP(this.canSleep);
                argoArch.setPort(this.portAddress);
            } else {
                argoArch.setCAN_SLEEP(this.canSleep);
                argoArch.setPort("/dev/" + this.portAddress);
            }
            return true;
        }else{
            ts.getLogger().warning("[WARNING] It was not possible to call internal action .act because this agent is not an Argo agent.");
            return false;
        }
        
    }

    /* helpers */
    private String unquote(String s) {
        if (s == null) return null;
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private boolean atomToBoolean(Atom atom) throws JasonException {
        if(atom.toString().equals("true")){
            return true;
        }else if(atom.toString().equals("false")){
            return false;
        }
        throw JasonException.createWrongArgument(this,
                "Second argument must be true or false.\n" +
                        " \t Consult https://github.com/chon-group/Argo/wiki/");
    }
}