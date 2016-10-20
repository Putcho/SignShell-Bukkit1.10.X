package net.putcho.bukkit.signshell;

import java.util.ArrayList;
import java.util.List;

public class SignShell {
	public static void main(String[] args){
		String[] src = {
				"Test i = new Test();",
				"i.hoge();"
				};
		SignShell ss = new SignShell(src);
		ss.readAll();
		for(String line: src){
			System.out.println(line);
		}
		ss.output();
		//Result
		/**
		 * Test i = new Test();
		 * i.hoge();
		 * ("Test", NAME)("i", NAME)("=", SYMBOL)("new", RESERVED)("Test", NAME)("(", BRACKET)(")", BRACKET)(";", SEMICOLON)
		 * ("i", NAME)(".", PERIOD)("hoge", NAME)("(", BRACKET)(")", BRACKET)(";", SEMICOLON)
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

	static final String[] symbols = {
			"+", "-", "*", "/", "=", "&", "|",
			"++", "+=", "--", "-=", "**", "*=", "/=", "==", "!=",
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

	void output(){
		for(Hoge hoge: words){
			System.out.print(hoge);
			if(hoge.type == Type.SEMICOLON)System.out.println();
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
		for(int i = 0; i < 7; i++){
			if(String.valueOf(read).equals(symbols[i]))return true;
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
	public String hoge;
	public Type type;
	public Hoge(String hoge, Type type){
		this.hoge = hoge;
		this.type = type;
	}
	@Override
	public String toString(){
		return String.format("(\"%s\", %s)", hoge, type);
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
