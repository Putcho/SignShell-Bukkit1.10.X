package net.putcho.bukkit.signshell;

import java.util.ArrayList;
import java.util.List;

public class SignShell {
	public static void main(String[] args){

	}

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
							if(read == ln){
								add(word.substring(0, word.length() - 1), Type.COMMENT);
								continue roop;
							}
						}
						add(word, Type.COMMENT);
						break roop;
					}else if(read == '*'){

					}
				}
			}
			if(Character.isWhitespace(read)){//空白文字無視

			}
		}
	}

	void add(String word){
		add(word, getType(word));
	}

	void add(String word, Type type){
		words.add(new Hoge(word, type));
		this.word = "";
	}

	Type getType(String word){
		return Type.NAME;
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
	COMMENT,
	SYMBOL,
	RESERVED,
	NAME,
}
