package net.putcho.bukkit.signshell;

import java.util.ArrayList;
import java.util.List;

public class SignShell {
	public static void main(String[] args){

	}

	static final String[] reservedWords = {
			"abstract",
			"boolean", "break", "byte",
			"case", "catch", "char", "class", "continue",
			"default", "do",
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
			",", "."
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
				this.src[cnt] = ln;
				cnt++;
			}
		}
		this.cnt = 0;
		this.word = "";
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
				}
			}

			int cha;
			if(isSymbol()){
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
				}
				if(cha < 6){
					this.add(String.valueOf(read), Type.BRACKET);
				}else if(cha < 7){
					this.add(String.valueOf(read), Type.COMMA);
				}else if(cha < 8){
					this.add(String.valueOf(read), Type.PERIOD);
				}
			}else if(Character.isWhitespace(read)){
				if(word.length() > 1){
					this.add(word.substring(0, word.length() - 1));
				}else{
					this.resetWord();
				}
			}
		}
	}

	void add(String word){
		add(word, getType(word));
	}

	void add(String word, Type type){
		words.add(new Hoge(word, type));
		this.resetWord();
	}

	void resetWord(){
		this.word = "";
	}

	Type getType(String word){
		return Type.NAME;
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
}

enum Type{
	SYMBOL,
	RESERVED,
	BRACKET,
	COMMA,
	PERIOD,
	NAME,
}
