package masa.tokenizer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;

public class Tokenizer {

	private ArrayList<String> targetFiles;
	private String commonPath;
	private String commonOutputPath;

	public Tokenizer(){ }

	public Tokenizer(String[] args) {
		targetFiles = new ArrayList<>();
		this.commonPath = preprocessPath(args[0]);
		this.commonOutputPath = preprocessPath(args[1]);
	}
	
	public String tokenizeFile(String source) throws InvalidInputException {
		IScanner scanner = ToolFactory.createScanner(false, false, true, "1.8");
		StringBuilder sb = new StringBuilder();
		scanner.setSource(source.toCharArray());
		int tokens;
		while ((tokens = scanner.getNextToken()) != ITerminalSymbols.TokenNameEOF) {
			String token = this.replaceEscapeChar(new String(scanner.getCurrentTokenSource()));
			sb.append(token);
			sb.append("\n");
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}
	
	private String replaceEscapeChar(String str) {
		
		String token = str.replaceAll(" ", "_").replaceAll("\t", "_").replaceAll("\r", "").replaceAll("\n", "");
		
		if (token.startsWith("'") && token.endsWith("'") && token.length() == 3) {
			token = token.substring(1, token.length() - 1);
			StringBuilder sb = new StringBuilder("'");
			for (int i = 0; i < token.length(); i++) {
				sb.append(String.format("\\u%04X", Character.codePointAt(token, i)));
			}
			sb.append("'");
			token = sb.toString();
		}
		
		return token;
	}

	private static void writeFile(final String text, final Path path) {
		try {
			final Path parentDir = path.getParent();
			if (!Files.exists(parentDir)) {
				Files.createDirectories(parentDir);
			}
			List<String> texts = new ArrayList<>();
			texts.add(text);
			Files.write(path, texts, StandardCharsets.UTF_8);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	
	public static void main(String[] args) throws Exception {
		new Tokenizer(args).run(args);
	}

	public void run(String[] args) throws InvalidInputException {
		String inputPath = preprocessPath(args[0]);
		String extension = ".java";
		storeFiles(inputPath, extension);
		for (String path : targetFiles) {
			File file = new File(path);
			String code = readFile(path);
			String tokenizedCode = "";
			if(file.length()!=0){
				tokenizedCode = tokenizeFile(code);
			} else {
				tokenizedCode = code;
			}
			String outputPath = this.commonOutputPath + path.replace(this.commonPath, "");
			writeFile(tokenizedCode, Paths.get(outputPath));
		}
	}

	private String preprocessPath(String path) {
		String home = System.getProperty("user.home");
		String inputPath = path;
		inputPath = inputPath.replace("~", home);
		File file = new File(inputPath);
		if (!file.isAbsolute()) {
			try {
				inputPath = Paths.get(inputPath).toRealPath(LinkOption.NOFOLLOW_LINKS).toString();
			} catch (NoSuchFileException ignored) {
			} catch (IOException e) {
				System.out.println(e);
			}
		}
		return inputPath;
	}

	private void storeFiles(String path, String extension) {
		if (path.endsWith(extension)) {
			String newFile = path;
			newFile = newFile.replaceAll("//", "/");
			targetFiles.add(newFile);
		} else {
			File dir = new File(path);
			File[] files = dir.listFiles();
			for (File file : Objects.requireNonNull(files)) {
				String file_name = file.getName();
				if (file.isDirectory()) {
					storeFiles(path + "/" + file_name, extension);
				} else {
					if (file_name.endsWith(extension)) {
						String newFile = path + "/" + file_name;
						newFile = newFile.replaceAll("//", "/");
						targetFiles.add(newFile);
					}
				}
			}
		}
	}

	private String readFile(String path) {
		StringBuilder builder = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
			String string = reader.readLine();
			while (string != null) {
				builder.append(string + System.getProperty("line.separator"));
				string = reader.readLine();
			}
		} catch (IOException e) {
			System.err.println(e);
		}
		return builder.toString();
	}

}