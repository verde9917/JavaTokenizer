package masa.tokenizer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class Options {
	
	private CmdLineParser parser;
	
	@Option(name = "-h", aliases = "--help", usage = "print usage message and exit")
	public boolean isShowUsage;
	
	@Option(name = "-i", aliases = "--input", usage = "input file or directory")
	public Path input;
	
	@Option(name = "-o", aliases = "--output", usage = "output file (default: stdin out)")
	public Path output;
	
	@Option(name = "-d", aliases = "--debug", usage = "debug mode ")
	public boolean isDebugMode;
	
	@Option(name = "--normalize", usage = "nomalize identifier")
	public boolean isNormalize;
	
	public boolean isOutputStdin = true;
	
	public ArrayList<Path> inputFiles = new ArrayList<Path>();
	
	public Options(String[] args) throws CmdLineException {
		this.parser = new CmdLineParser(this);
		this.parser.parseArgument(args);
		if (args.length == 0) this.printUsage();
		this.checkOptions();
	}
	
	public Options() {
	}
	
	private void checkOptions() {
		if (this.isShowUsage) this.printUsage();
		this.isOutputStdin = (output == null);
	}
	
	public void printUsage() {
		System.out.print("Usage:");
		System.out.println(" Tokenizer [options]");
		System.out.println();
		System.out.println("Options:");
		parser.printUsage(System.out);
		System.exit(0);
	}
	
	public void setInputFiles() {
		System.err.println("### file searching ...");
		ArrayList<Path> files = new ArrayList<>();
		if (Files.isDirectory(input)) {
			files = this.recursiveSearchInDirectory(files, input);
		} else {
			if (input.toString().endsWith(".java")) files.add(input);
		}
		this.inputFiles = files;
		
	}
	
	private ArrayList<Path> recursiveSearchInDirectory(ArrayList<Path> inputFiles, Path input) {
		if (!Files.exists(input)) return inputFiles;
		if (Files.isDirectory(input)) {
			System.err.println(input);
			for (File child : input.toFile().listFiles()) {
				recursiveSearchInDirectory(inputFiles, child.toPath());
			}
			return inputFiles;
		} else {
			if (input.toString().endsWith(".java")) {
				inputFiles.add(input);
			}
		}
		return inputFiles;
	}
	
}
