/* This file was generated by SableCC (http://www.sablecc.org/). */

package net.sourceforge.texlipse.texparser.node;

import net.sourceforge.texlipse.texparser.analysis.*;

public final class TCssection extends Token
{
    public TCssection()
    {
        super.setText("\\subsection");
    }

    public TCssection(int line, int pos)
    {
        super.setText("\\subsection");
        setLine(line);
        setPos(pos);
    }

    public Object clone()
    {
      return new TCssection(getLine(), getPos());
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTCssection(this);
    }

    public void setText(String text)
    {
        throw new RuntimeException("Cannot change TCssection text.");
    }
}