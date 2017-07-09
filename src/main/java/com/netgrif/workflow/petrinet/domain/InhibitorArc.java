package com.netgrif.workflow.petrinet.domain;

public class InhibitorArc extends Arc {
    @Override
    public boolean isExecutable() {
        if (source instanceof Transition)
            return true;
        return ((Place) source).getTokens() < multiplicity;
    }

    @Override
    public void execute(){ }
}