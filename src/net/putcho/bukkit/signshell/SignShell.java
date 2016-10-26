package net.putcho.bukkit.signshell;

import java.util.ArrayList;
import java.util.List;

public class SignShell {
	public static void main(String[] args){
		String[] src = {
				"int i = 10 * ((8 + 4) | 5) + 3 * ((18 / (5 + 4)) + 3);",
//				"i += 5;",
//				"message(i);"
				};
		SignShell ss = new SignShell(src);
		ss.readAll();
		for(String line: src){
			System.out.println(line);
		}
		ss.output();
		CodeAnalysis ca = new CodeAnalysis(ss.getCodes());
		ca.readAll();
		System.out.println(10 * ((8 + 4) | 5) + 3 * ((18 / (5 + 4)) + 3));
		//Result
		/**
		 * int i = 10 * ((8 + 4) | 5) + 3 * ((18 / (5 + 4)) + 3);
		 * ("int", RESERVED)("i", NAME)("=", SYMBOL)("10", INTEGER)("*", SYMBOL)("(", BRACKET)("(", BRACKET)("8", INTEGER)("+", SYMBOL)("4", INTEGER)(")", BRACKET)("|", SYMBOL)("5", INTEGER)(")", BRACKET)("+", SYMBOL)("3", INTEGER)("*", SYMBOL)("(", BRACKET)("(", BRACKET)("18", INTEGER)("/", SYMBOL)("(", BRACKET)("5", INTEGER)("+", SYMBOL)("4", INTEGER)(")", BRACKET)(")", BRACKET)("+", SYMBOL)("3", INTEGER)(")", BRACKET)(";", SEMICOLON)
		 * 145
		 * 145
		 */
	}

	static final String[] reservedWords = {
			"abstract",
			"boolean", "break", "byte",
			"case", "catch", "char", "class", "continue",
			"default", "do", "double",
			"else", "extends",
			"false", "final", "float", "for",
			"implements", "import", "int", "interface",
			"new", "null",
			"package", "private", "protected", "public",
			"return",
			"short", "static", "super", "switch", "synchronized",
			"this", "throws", "try", "true",
			"void",
			"while",
	};

	static final String[] singleSymbols = {
			"+", "-", "*", "/", "%", "=", "&", "|",
	};

	static final String[] symbols = {
			"++", "+=", "--", "-=", "**", "*=", "/=", "%=", "==", "!=",
			"&&", "||", "&=", "|=",
	};

	static final String[] chars = {
			"{", "}",
			"[", "}",
			"(", ")",
			",", ":", ";"
	};

	private char ln;

	private char[] src;
	private List<Hoge> words;

	private String word;
	private char read;

	private int cnt;

	public SignShell(String[] src){
		ln = System.getProperty("line.separator").charAt(0);
		words = new ArrayList<Hoge>();

		int le = 0;
		for(String s: src){
			le += s.length();
			le++;
		}
		this.src = new char[le];
		int cnt = 0;
		for(String s: src){
			for(char c: s.toCharArray()){
				this.src[cnt] = c;
				cnt++;
			}
			this.src[cnt] = ln;
			cnt++;
		}
		this.cnt = 0;
		this.word = "";
	}

	Hoge[] getCodes(){
		return words.toArray(new Hoge[0]);
	}

	void output(){
		for(Hoge hoge: words){
			System.out.print(hoge);
			if(hoge.getType() == Type.SEMICOLON)System.out.println();
		}
	}

	boolean read(){
		if(cnt < src.length){
			read = src[cnt];
			word += read;
			cnt++;
			return true;
		}
		return false;
	}

	public void readAll(){
		roop: while(read()){
			if(read == '/'){
				if(read()){
					if(read == '/'){
						while(read()){
							if(read == ln)break;
						}
						this.resetWord();
						continue roop;
					}else if(read == '*'){
						while(read()){
							if(word.matches("/\\*.*\\*/"))break;
						}
						this.resetWord();
						continue roop;
					}else{
						cnt -= 2;
						resetWord();
						read();
					}
				}else{
					break roop;
				}
			}

			int cha;
			if(isSymbol()){
				if(word.length() > 1){
					add(word.substring(0, word.length() - 1));
					word = String.valueOf(read);
				}
				if(read()){
					for(String symbol: symbols){
						if(word.equals(symbol)){
							this.add(word, Type.SYMBOL);
							continue roop;
						}
					}
					this.add(word.substring(0, word.length() - 1), Type.SYMBOL);
					word = String.valueOf(read);
				}else{
					break roop;
				}
			}
			if((cha = getChartype()) < chars.length){
				if(word.length() > 1){
					this.add(word.substring(0, word.length() - 1));
					word = String.valueOf(read);
				}
				if(cha < 6){
					this.add(word, Type.BRACKET);
				}else if(cha < 7){
					this.add(word, Type.COMMA);
				}else if(cha < 8){
					this.add(word, Type.COLON);
				}else if(cha < 9){
					this.add(word, Type.SEMICOLON);
				}
			}else if(Character.isWhitespace(read)){
				if(word.length() > 1){
					this.add(word.substring(0, word.length() - 1));
				}else{
					this.resetWord();
				}
			}
		}
		if(word.length() > 0){
			add(word);
		}
	}

	void add(String word){
		addClassify(word);
	}

	void add(String word, Type type){
		words.add(new Hoge(word, type));
		this.resetWord();
	}

	void resetWord(){
		this.word = "";
	}

	Type getType(String word){
		for(String reserved: reservedWords){
			if(word.equals(reserved))return Type.RESERVED;
		}
		return Type.NAME;
	}

	void addClassify(String src){
		String[] space = src.split("\\s+");
		for(String word: space){
			if(word.equals(".")){
				add(word, Type.PERIOD);
			}else if(word.matches("\\d+")){
				add(word, Type.INTEGER);
			}else if(word.matches("\\d+\\.\\d+")
					|| word.matches("\\.\\d+")
					|| word.matches("\\d+\\.")){
				add(word, Type.DOUBLE);
			}else{
				String[] period = word.split("\\.");
				if(period.length == 1){
					if(word.matches(".+\\.")){
						String w = word.substring(0, word.length() - 1);
						add(w, getType(w));
						add(".", Type.PERIOD);
					}else{
						add(word, getType(word));
					}
				}else{
					for(int i = 0; i < period.length; i++){
						if(i != 0){
							add(".", Type.PERIOD);
						}
						if(!period[i].isEmpty()){
							add(period[i], getType(period[i]));
						}
					}
				}
			}
		}
	}

	boolean isSymbol(){
		for(int i = 0; i < singleSymbols.length; i++){
			if(String.valueOf(read).equals(singleSymbols[i]))return true;
		}
		return false;
	}

	int getChartype(){
		int cnt = 0;
		while(cnt < chars.length){
			if(chars[cnt].charAt(0) == read)break;
			cnt++;
		}
		return cnt;
	}
}

class Hoge{
	private String word;
	private Type type;
	public Hoge(String word, Type type){
		this.word = word;
		this.type = type;
	}

	public String getWord(){
		return word;
	}

	public Type getType(){
		return type;
	}

	@Override
	public String toString(){
		return String.format("(\"%s\", %s)", word, type);
	}
}

enum Type{
	SYMBOL,
	RESERVED,
	BRACKET,
	COMMA,
	PERIOD,
	COLON,
	SEMICOLON,
	NAME,
	INTEGER,
	DOUBLE
}
