package net.putcho.bukkit.signshell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeAnalysis {
	private Hoge[] codes;
	private Hoge read;
	private int cnt;

	private List<Hoge> temp;

	private Map<String, Class<?>> vars;
	private Map<String, Object> objmanager;

	private static Map<String, String> wrapper;

	static{
		wrapper = new HashMap<String, String>();

		wrapper.put("int", "Integer");
		wrapper.put("double", "Double");
		wrapper.put("char", "Character");
	}

	public CodeAnalysis(Hoge[] codes){
		this.codes = codes;
		this.cnt = 0;
		temp = new ArrayList<Hoge>();
		vars = new HashMap<String, Class<?>>();
		objmanager = new HashMap<String, Object>();
	}

	private boolean read(){
		if(cnt < codes.length){
			read = codes[cnt++];
			return true;
		}
		return false;
	}

	public void readAll(){
		while(read()){
			if(read.getType().equals(Type.SEMICOLON)){
				runEstimation();
				temp.clear();
			}else{
				if(read.getType().equals(Type.RESERVED)){
					temp.add(checkWrapper());
				}else{
					temp.add(read);
				}
			}
		}
	}

	private boolean runEstimation(){
		if(temp.size() > 1){
			int pattern = getPattern();
			if(pattern == -1)return false;
			if(pattern <= 1){
				String claz = temp.get(0).getWord();
				String var = temp.get(1).getWord();

				Class<?> clazz = getNameClass(claz);
				if(clazz == null)return false;
				vars.put(var, clazz);
				if(pattern == 1){
					String sym = temp.get(2).getWord();
					if(!sym.equals("="))return false;
					ReturnObj ro = getAssObj(temp.subList(3, temp.size()));
					if(ro == null)return false;
					if(!vars.get(var).equals(ro.clazz))return false;
					objmanager.put(var, ro.obj);
					System.out.println(ro.obj);
				}
			}
		}
		return false;
	}

	private Hoge checkWrapper(){
		if(wrapper.containsKey(read.getWord())){
			return new Hoge(wrapper.get(read.getWord()), Type.NAME);
		}
		return read;
	}

	private ReturnObj getAssObj(List<Hoge> ass){
		Hoge h = hoge(new ReadArray<Hoge>(ass));
		if(h == null)return null;
		if(h.getType().equals(Type.INTEGER)){
			return new ReturnObj(Integer.class, Integer.parseInt(h.getWord()));
		}else{
			return new ReturnObj(Double.class, Double.parseDouble(h.getWord()));
		}
	}

	private Hoge hoge(ReadArray<Hoge> hoge){
		List<Hoge> cp = new ArrayList<Hoge>();
		Hoge h;
		while((h = hoge.read()) != null){
			if(h.getType().equals(Type.BRACKET)){
				if(h.getWord().equals("(")){
					cp.add(hoge(hoge));
				}else if(h.getWord().equals(")")){
					return calc(cp);
				}
			}else{
				cp.add(h);
			}
		}
		return calc(cp);
	}

	private Hoge calc(List<Hoge> hoges){
		int flag = 0;

		List<Hoge> ho = new ArrayList<Hoge>();
		Hoge h;
		try{
			for(int i = 0; i < hoges.size(); i++){
				h = hoges.get(i);
				if(h.getType().equals(Type.SYMBOL)
						&& (h.getWord().equals("*") || h.getWord().equals("/") || h.getWord().equals("%"))){
					if(i == 0 || i + 1 == hoges.size())return null;
					Hoge f = ho.get(ho.size() - 1);
					Hoge s = hoges.get(++i);
					if(flag == 0){
						flag |= f.getType().equals(Type.INTEGER)? 0: 1;
						flag |= s.getType().equals(Type.INTEGER)? 0: 1;
					}
					Object var;
					if(h.getWord().equals("*")){
						if(flag == 0){
							var = Integer.parseInt(f.getWord()) * Integer.parseInt(s.getWord());
						}else{
							var = Double.parseDouble(f.getWord()) * Double.parseDouble(s.getWord());
						}
					}else if(h.getWord().equals("/")){
						if(flag == 0){
							var = Integer.parseInt(f.getWord()) / Integer.parseInt(s.getWord());
						}else{
							var = Double.parseDouble(f.getWord()) / Double.parseDouble(s.getWord());
						}
					}else{
						if(flag == 0){
							var = Integer.parseInt(f.getWord()) % Integer.parseInt(s.getWord());
						}else{
							var = Double.parseDouble(f.getWord()) % Double.parseDouble(s.getWord());
						}
					}
					ho.set(ho.size() - 1, new Hoge(var.toString(), flag == 0? Type.INTEGER: Type.DOUBLE));
				}else{
					ho.add(h);
				}
			}
			List<Hoge> ho1 = new ArrayList<Hoge>();
			for(int i = 0; i < ho.size(); i++){
				h = ho.get(i);
				if(h.getType().equals(Type.SYMBOL)
						&& (h.getWord().equals("+") || h.getWord().equals("-"))){
					if(i == 0 || i + 1 == ho.size())return null;
					Hoge f = ho1.get(ho1.size() - 1);
					Hoge s = ho.get(++i);
					if(flag == 0){
						flag |= f.getType().equals(Type.INTEGER)? 0: 1;
						flag |= s.getType().equals(Type.INTEGER)? 0: 1;
					}
					Object var;
					if(h.getWord().equals("+")){
						if(flag == 0){
							var = Integer.parseInt(f.getWord()) + Integer.parseInt(s.getWord());
						}else{
							var = Double.parseDouble(f.getWord()) + Double.parseDouble(s.getWord());
						}
					}else{
						if(flag == 0){
							var = Integer.parseInt(f.getWord()) - Integer.parseInt(s.getWord());
						}else{
							var = Double.parseDouble(f.getWord()) - Double.parseDouble(s.getWord());
						}
					}
					ho1.set(ho1.size() - 1, new Hoge(var.toString(), flag == 0? Type.INTEGER: Type.DOUBLE));
				}else{
					ho1.add(h);
				}
			}
			List<Hoge> ho2 = new ArrayList<Hoge>();
			for(int i = 0; i < ho1.size(); i++){
				h = ho1.get(i);
				if(h.getType().equals(Type.SYMBOL)
						&& (h.getWord().equals("&") || h.getWord().equals("|"))){
					if(i == 0 || i + 1 == ho1.size())return null;
					Hoge f = ho2.get(ho2.size() - 1);
					Hoge s = ho1.get(++i);
					if(flag == 0){
						flag |= f.getType().equals(Type.INTEGER)? 0: 1;
						flag |= s.getType().equals(Type.INTEGER)? 0: 1;
					}
					if(flag == 1)return null;
					Object var;
					if(h.getWord().equals("&")){
						var = Integer.parseInt(f.getWord()) & Integer.parseInt(s.getWord());
					}else{
						var = Integer.parseInt(f.getWord()) | Integer.parseInt(s.getWord());
					}
					ho2.set(ho2.size() - 1, new Hoge(var.toString(), flag == 0? Type.INTEGER: Type.DOUBLE));
				}else{
					ho2.add(h);
				}
			}
			return ho2.get(0);
		}catch(NumberFormatException e){
			return null;
		}
	}

	private Class<?> getNameClass(String name){
		if(name.equals("Integer")){
			return Integer.class;
		}else if(name.equals("Double")){
			return Double.class;
		}else if(name.equals("Character")){
			return Character.class;
		}else if(name.equals("String")){
			return String.class;
		}
		try {
			Class<?> clazz = Class.forName("net.putcho.bukkit.signshell.lang." + name);
			return clazz;
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	private int getPattern(){
		if(temp.get(0).getType().equals(Type.NAME)
				&& temp.get(1).getType().equals(Type.NAME)){
			//変数宣言
			if(temp.size() == 2)return 0;
			//変数宣言&代入
			if(temp.size() >= 4 && temp.get(2).getType().equals(Type.SYMBOL))return 1;
		}else if(temp.size() >= 3
				&& temp.get(0).getType().equals(Type.NAME)
				&& temp.get(1).getType().equals(Type.SYMBOL)){
			//代入演算
			return 2;
		}
		return -1;
	}

	private class ReturnObj{
		Class<?> clazz;
		Object obj;
		private ReturnObj(Class<?> clazz, Object obj){
			this.clazz = clazz;
			this.obj = obj;
		}
	}

	private class ReadArray<T>{
		int i;
		List<T> list;
		private ReadArray(List<T> list){
			this.list = list;
			i = 0;
		}

		private T read(){
			if(i < list.size()){
				return list.get(i++);
			}
			return null;
		}
	}
}
