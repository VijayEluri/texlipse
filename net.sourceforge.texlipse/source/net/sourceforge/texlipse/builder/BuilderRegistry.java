/*
 * $Id$
 *
 * Copyright (c) 2004-2005 by the TeXlapse Team.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package net.sourceforge.texlipse.builder;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.texlipse.TexlipsePlugin;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;


/**
 * Holds a registry of available builders and runners.
 * Implemented using the Singleton pattern.
 * 
 * @author Kimmo Karlsson
 * @author Boris von Loesch
 */
public class BuilderRegistry {

	
	public static final String LATEX_RUNNER_ID = "latex";
	public static final String PSLATEX_RUNNER_ID = "pslatex";
	public static final String PDFLATEX_RUNNER_ID = "pdflatex";
	public static final String DVIPS_RUNNER_ID = "dvips";
	public static final String DVIPDF_RUNNER_ID = "dvipdf";
	public static final String PS2PDF_RUNNER_ID = "ps2pdf";
	public static final String XELATEX_RUNNER_ID = "xelatex";
	public static final String LUALATEX_RUNNER_ID = "lualatex";
	
	public static final String BIBTEX_RUNNER_ID = "bibtex";
	public static final String BIBER_RUNNER_ID = "biber";
	public static final String MAKEINDEX_RUNNER_ID = "makeindex";
	public static final String MAKEINDEX_NOMENCL_RUNNER_ID = "makeindex.nomencl";
	
	
    // the singleton instance
    private static BuilderRegistry instance = new BuilderRegistry();
    
    // registry of outputformat -> builder mappings
    private Builder[] builderList;
    
    // array of program runners
    private ProgramRunner[] runnerList;


    private static final Object consoleLock = new Object();
    
    // stream to write builder status messages to
    private static MessageConsoleStream consoleStream;

    // the console for creating console streams
    private static MessageConsole console;


    /**
     * Print a message to the console.
     */
    public static void printToConsole(String msg) {
        getConsoleStream().println(msg);
    }
    
    /**
     * Clear the console window.
     */
    public static void clearConsole() {
        synchronized (consoleLock) {
            consoleStream = null;
        }
        TexlipsePlugin.getDefault().getWorkbench().getDisplay().syncExec(new Runnable() {
            public void run() {
                MessageConsole c;
                synchronized (consoleLock) {
                    c = console;
                }
                if (c != null) {
                    c.getDocument().set("");
                }
            }});
    }
    
    /**
     * Return the console. Instantiate if necessary.
     * @return the output console
     */
    private static MessageConsole getConsole() {
        synchronized (consoleLock) {
            if (console == null) {
                console = new MessageConsole("Texlipse", null);
                IConsoleManager mgr = ConsolePlugin.getDefault().getConsoleManager();
                mgr.addConsoles(new IConsole[] { console });
            }
                return console;
        }
    }
    
    /**
     * Return the console output stream. Instantiate if necessary.
     * @return the output stream to console
     */
    private static MessageConsoleStream getConsoleStream() {
        synchronized (consoleLock) {
            if (consoleStream == null) {
                consoleStream = getConsole().newMessageStream();
            }
            return consoleStream;
        }
    }
    
    
    /**
     * Get the builder for the given format.
     * @param clazz the class of the builder
     * @param format the output format
     * 
     * @return the builder or null if there is no such builder configured
     */
    public static Builder get(Class<? extends Builder> clazz, String format) {
        return instance.getBuilder(clazz, format);
    }
    
    /**
     * Find all builders that produce the given output format.
     * 
     * @param format output format
     * @return references to the builder instances
     */
    public static Builder[] getAll(String format) {
        return instance.getAllBuilders(format);
    }

    /**
     * Get the ith builder
     * @param i id number of the builder
     * @return the builder instance, or null if index out of bounds
     */
    public static Builder get(int i) {
        return instance.getBuilder(i);
    }
    
    /**
     * Get the ith program runner.
     * 
     * @param i the index of a runner
     * @return a program runner
     */
    public static ProgramRunner getRunner(int i) {
        return instance.getProgramRunner(i);
    }

    /**
     * Hidden constructor.
     * Creates the shared instances of the program runners and the builders.
     * 
     * We could use lazy instantiation, but the ProgramRunners don't allocate
     * much memory at construction time and their construction requires almost
     * no processing, so it is easier to allocate them all at once.
     * 
     * Also: - we need all the runners when constructing builders
     *       - we need the number of runners when initializing preferences
     */
    protected BuilderRegistry() {
        runnerList = new ProgramRunner[] {
                new LatexRunner(),
                new PslatexRunner(),
                new PdflatexRunner(),
                new XelatexRunner(),
                new LualatexRunner(),
                new BibtexRunner(),
                new BiberRunner(),
                new MakeindexRunner(),
                new DvipsRunner(),
                new DvipdfRunner(),
                new Ps2pdfRunner(),
                new MakeindexNomenclRunner(),
                new KpsewhichRunner()
        };
    }

    /**
     * Initializes builders. This cannot be done in constructor,
     * because builders need to use BuilderRegistry to resolve runners.
     * @return 
     */
    protected synchronized Builder[] getBuilders() {
        if (builderList == null) {
            builderList = new Builder[9];
            builderList[0] = new TexBuilder(0, LATEX_RUNNER_ID);
            builderList[1] = new TexBuilder(1, PSLATEX_RUNNER_ID);
            builderList[2] = new TexBuilder(2, PDFLATEX_RUNNER_ID);
            
            builderList[3] = new DviBuilder(3, DVIPS_RUNNER_ID);
            builderList[4] = new DviBuilder(4, DVIPDF_RUNNER_ID);
            
            builderList[5] = new PsBuilder(5, TexBuilder.class);
            builderList[6] = new PsBuilder(6, DviBuilder.class);
            
            builderList[7] = new TexBuilder(7, XELATEX_RUNNER_ID);
            builderList[8] = new TexBuilder(8, LUALATEX_RUNNER_ID);
        }
        return builderList;
    }
    
    /**
     * Find a builder from the registry for the given output format.
     *  
     * @param builderClass the implementing class of the builder
     * @param outputFormat the output format of the builder
     * @return the builder instance, or null if none found
     */
	protected Builder getBuilder(Class<? extends Builder> builderClass, String outputFormat) {
        if (outputFormat == null) {
            return null;
        }

        Builder builder = null;
        final Builder[] builderList = getBuilders();
        for (int i = 0; i < builderList.length; i++) {
            if (builderList[i] != null
                    && builderList[i].getOutputFormat().equals(outputFormat)
                    && (builderClass == null || builderList[i].getClass().equals(builderClass))) {
                builder = builderList[i];
            }
        }
        
        if (builder == null) {
            return null;
        }
        
        return builder;
    }

    /**
     * Find all builders that produce the given output format.
     * 
     * @param format the output format
     * @return references to the builders
     */
    protected Builder[] getAllBuilders(String format) {

        List<Builder> list = new ArrayList<Builder>();

        final Builder[] builderList = getBuilders();
        for (int i = 0; i < builderList.length; i++) {
            if (builderList[i] != null
                    && builderList[i].getOutputFormat().equals(format)) {
                list.add(builderList[i]);
            }
        }

        return list.toArray(new Builder[0]);
    }
    
    /**
     * Get the ith program runner.
     * 
     * @param i the index number of the runner
     * @return the program runner
     */
    protected ProgramRunner getProgramRunner(int i) {
        if (i >= 0 && i < runnerList.length) {
            return runnerList[i];
        }
        return null;
    }
    
    protected Builder getBuilder(int i) {
        final Builder[] builderList = getBuilders();
        if (i >= 0 && i < builderList.length) {
            return builderList[i];
        }
        return null;
    }
    
    /**
     * Returns the number of implemented runners.
     * @return the number of implemented runners
     */
    public static int getNumberOfRunners() {
        return instance.runnerList.length;
    }
    
	public static ProgramRunner getRunner(String id) {
		for (ProgramRunner runner : instance.runnerList) {
			if (runner.getId().equals(id)) {
				return runner;
			}
		}
		return null;
	}
}

