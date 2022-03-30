package com.thiendz.lib.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class JJson implements Comparable<JJson> {
	private 
	private Object json;

	public static JJson parse(String json) {
		String a = "1999";
		return new JJson(json);
	}

	public static JJson parse(Object json) {
		return new JJson(json);
	}

	public static JJson build() {
		return new JJson();
	}

	public JJson() {
	}

	public JJson(Object json) {
		this.json = json;
	}

	public JJson(String json) {
		if (json != null) {
			this.json = JSONValue.parse(json);
		}
	}

	public JJson k(String key) {
		JSONObject jsonObj = toJsonObject(json);
		return new JJson(jsonObj != null ? jsonObj.get(key) : null);
	}

	public JJson i(int index) {
		JSONArray jsonArray = toJsonArray(json);
		return new JJson(jsonArray != null ? jsonArray.get(index) : null);
	}

	public JJson q(String query) {
		final String REGEX_JSON_VALID = "^((\\.\\w+(\\[\\d+\\])*)|((\\[\\d+\\])*))+$";
		final String REGEX_JSON_NAME = "^(\\w+)\\[";
		final String REGEX_JSON_KEY_ARRAY = "\\[(\\d+)\\]+";
		if (json == null) {
			return this;
		}
		if (stringRegex(REGEX_JSON_VALID, query) == null) {
			return new JJson("");
		}
		Object jsonTemp = json;
		String[] querys = query.split("\\.");
		for (String node : querys) {
			if (node.equals("")) {
				continue;
			}
			if (query.contains("[")) { 
				if (!query.startsWith("[")) { 
					String[] names = stringRegex(REGEX_JSON_NAME, node);
					String name = names[0].replace("[", "");
					jsonTemp = toJsonObject(jsonTemp).get(name); 
				}
				String[] strIndexs = stringRegex(REGEX_JSON_KEY_ARRAY, node);
				for (String strIndex : strIndexs) {
					int index = Integer.parseInt(strIndex.replaceAll("\\[|\\]", ""));
					JSONArray jsonArrayTemp = toJsonArray(jsonTemp);
					jsonTemp = jsonArrayTemp != null ? jsonArrayTemp.get(index) : null;
				}
			} else {
				JSONObject jsonObjectTemp = toJsonObject(jsonTemp);
				jsonTemp = jsonObjectTemp != null ? jsonObjectTemp.get(node) : null;
			}
		}
		return new JJson(jsonTemp);
	}

	public JJson put(Map<?, ?> map) {
		JSONObject jsonObject = json == null ? new JSONObject() : (JSONObject) json;
		if (map != null) {
			jsonObject.putAll(map);
		}
		return new JJson(jsonObject);
	}

	public JJson put(List<?> list) {
		JSONArray jsonArray = json == null ? new JSONArray() : (JSONArray) json;
		if (list != null) {
			jsonArray.addAll(list);
		}
		return new JJson(jsonArray);
	}

	public JJson min() {
		float[] fls = toFloats();
		if (fls == null) {
			return new JJson(null);
		}
		Arrays.sort(fls);
		return new JJson((Object) fls[0]);
	}

	public JJson max() {
		float[] fls = toFloats();
		if (fls == null) {
			return new JJson(null);
		}
		Arrays.sort(fls);
		final int flsLen = fls.length;
		return new JJson((Object) fls[flsLen - 1]);
	}

	public JJson sum() {
		float[] fls = toFloats();
		if (fls == null) {
			return new JJson(null);
		}
		return new JJson((Object) sumFloat(fls));
	}

	public JJson avg() {
		float[] fls = toFloats();
		if (fls == null) {
			return new JJson(null);
		}
		final int flsLen = fls.length;		
		return new JJson((Object) (sumFloat(fls) / flsLen));
	}

	public JJson sort() {
		JJson[] jjsons = toObjs();
		if (jjsons == null) {
			return new JJson(null);
		}
		Arrays.sort(jjsons);
		return new JJson(JSONValue.parse(arraysToString(jjsons)));
	}

	public JJson reverse() {
		JJson[] jjsons = toObjs();
		for (int i = 0; i < jjsons.length / 2; i++) {
			int idMax = jjsons.length - 1 - i;
			JJson json2T = jjsons[i];
			jjsons[i] = jjsons[idMax];
			jjsons[idMax] = json2T;
		}
		return new JJson(JSONValue.parse(arraysToString(jjsons)));
	}

	public JJson[][] toPairObjs() {
		JSONObject jsonObject = toJsonObject(json);
		if (jsonObject == null) {
			return null;
		}
		JJson[][] jsonPairs = new JJson[jsonObject.size()][2];
		Iterator iterator = jsonObject.entrySet().iterator();
		int i = 0;
		while (iterator.hasNext()) {
			Map.Entry pair = (Map.Entry) iterator.next();
			jsonPairs[i][0] = new JJson(pair.getKey());
			jsonPairs[i++][1] = new JJson(pair.getValue());
		}
		return jsonPairs;
	}

	public String[] toKeys() {
		JJson[][] jjsons = toPairObjs();
		if (jjsons == null) {
			return null;
		}
		String[] keys = new String[jjsons.length];
		int i = 0;
		for (JJson[] jjson : jjsons) {
			keys[i++] = jjson[0].toString();
		}
		return keys;
	}

	public JJson[] toValues() {
		JJson[][] jjsons = toPairObjs();
		if (jjsons == null) {
			return null;
		}
		JJson[] values = new JJson[jjsons.length];
		int i = 0;
		for (JJson[] jjson : jjsons) {
			values[i++] = jjson[1];
		}
		return values;
	}

	public JJson[] toObjs() {
		JSONArray jsonArray = toJsonArray(json);
		if (jsonArray == null) {
			return null;
		}
		JJson[] jjsons = new JJson[jsonArray.size()];
		for (int i = 0; i < jsonArray.size(); i++) {
			jjsons[i] = new JJson(jsonArray.get(i));
		}
		return jjsons;
	}

	public String[] toStrs() {
		JSONArray jsonArray = toJsonArray(json);
		if (jsonArray == null) {
			return null;
		}
		String[] strings = new String[jsonArray.size()];
		for (int i = 0; i < jsonArray.size(); i++) {
			strings[i] = jsonArray.get(i).toString();
		}
		return strings;
	}

	public char[] toChars() {
		JSONArray jsonArray = toJsonArray(json);
		if (jsonArray == null) {
			return null;
		}
		char[] chars = new char[jsonArray.size()];
		for (int i = 0; i < jsonArray.size(); i++) {
			char charr = '0';
			try {
				charr = jsonArray.get(i).toString().toCharArray()[0];
			} catch (ArrayIndexOutOfBoundsException e) {
			}
			chars[i] = charr;
		}
		return chars;
	}

	public int[] toInts() {
		JSONArray jsonArray = toJsonArray(json);
		if (jsonArray == null) {
			return null;
		}
		int[] ints = new int[jsonArray.size()];
		for (int i = 0; i < jsonArray.size(); i++) {
			int intt = 0;
			try {
				intt = Integer.parseInt(strToInt(jsonArray.get(i).toString()));
			} catch (NumberFormatException e) {
			}
			ints[i] = intt;
		}
		return ints;
	}

	public long[] toLongs() {
		JSONArray jsonArray = toJsonArray(json);
		if (jsonArray == null) {
			return null;
		}
		long[] longs = new long[jsonArray.size()];
		for (int i = 0; i < jsonArray.size(); i++) {
			long longg = 0;
			try {
				longg = Long.parseLong(strToInt(jsonArray.get(i).toString()));
			} catch (NumberFormatException e) {
			}
			longs[i] = longg;
		}
		return longs;
	}

	public double[] toDoubles() {
		JSONArray jsonArray = toJsonArray(json);
		if (jsonArray == null) {
			return null;
		}
		double[] doubles = new double[jsonArray.size()];
		for (int i = 0; i < jsonArray.size(); i++) {
			double doublee = 0;
			try {
				doublee = Double.parseDouble(jsonArray.get(i).toString());
			} catch (NumberFormatException e) {
			}
			doubles[i] = doublee;
		}
		return doubles;
	}

	public float[] toFloats() {
		JSONArray jsonArray = toJsonArray(json);
		if (jsonArray == null) {
			return null;
		}
		float[] floats = new float[jsonArray.size()];
		for (int i = 0; i < jsonArray.size(); i++) {
			float floatt = 0;
			try {
				floatt = Float.parseFloat(jsonArray.get(i).toString());
			} catch (NumberFormatException e) {
			}
			floats[i] = floatt;
		}
		return floats;
	}

	public boolean[] toBools() {
		JSONArray jsonArray = toJsonArray(json);
		if (jsonArray == null) {
			return null;
		}
		boolean[] booleans = new boolean[jsonArray.size()];
		for (int i = 0; i < jsonArray.size(); i++) {
			boolean bool = false;
			try {
				bool = Boolean.parseBoolean(jsonArray.get(i).toString());
			} catch (Exception e) {
			}
			booleans[i] = bool;
		}
		return booleans;
	}

	public Object toObj() {
		return json;
	}

	public String toStr() {
		return json == null ? null : json.toString();
	}

	@Override
	public String toString() {
		return toStr();
	}

	public char toChar() {
		try {
			return json.toString().toCharArray()[0];
		} catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
			return '0';
		}
	}

	public int toInt() {
		try {
			return Integer.parseInt(strToInt(json.toString()));
		} catch (NumberFormatException | NullPointerException e) {
			return 0;
		}
	}

	public long toLong() {
		try {
			return Long.parseLong(strToInt(json.toString()));
		} catch (NumberFormatException | NullPointerException e) {
			return 0;
		}
	}

	public double toDouble() {
		try {
			return Double.parseDouble(json.toString());
		} catch (NumberFormatException | NullPointerException e) {
			return 0;
		}
	}

	public float toFloat() {
		try {
			return Float.parseFloat(json.toString());
		} catch (NumberFormatException | NullPointerException e) {
			return 0;
		}
	}

	public boolean toBool() {
		try {
			return Boolean.parseBoolean(json.toString());
		} catch (NumberFormatException | NullPointerException eS) {
			return false;
		}
	}

	public int length() {
		int lengthObject = isInstanceOfJsonObject(json) ? toJsonObject(json).size() : -1;
		int lengthArray = isInstanceOfJsonArray(json) ? toJsonArray(json).size() : -1;
		return lengthArray > lengthObject ? lengthArray : lengthObject;
	}

	@Override
	public int compareTo(JJson jjson) {
		if (json == null) {
			return -1;
		}
		if (jjson == null) {
			return 1;
		}
		String oThis = this.toString();
		String oThat = jjson.toString();
		if (isNumber(oThis) && isNumber(oThat)) {
			return Float.parseFloat(oThis) > Float.parseFloat(oThat) ? 1 : -1;
		} else {
			int len1 = oThis.length();
			int len2 = oThat.length();
			int lim = Math.min(len1, len2);
			char v1[] = oThis.toCharArray();
			char v2[] = oThat.toCharArray();
			int k = 0;
			while (k < lim) {
				char c1 = v1[k];
				char c2 = v2[k];
				if (c1 != c2) {
					return c1 - c2;
				}
				k++;
			}
			return len1 - len2;
		}

	}

	//
	private static JSONObject toJsonObject(Object json) {
		return isInstanceOfJsonObject(json) ? (JSONObject) JSONValue.parse(json.toString()) : null;
	}

	private static JSONArray toJsonArray(Object json) {
		return isInstanceOfJsonArray(json) ? (JSONArray) JSONValue.parse(json.toString()) : null;
	}

	//
	private static boolean isInstanceOfJsonObject(Object json) {
		return json == null ? false : json instanceof JSONObject || json instanceof JJson;
	}

	private static boolean isInstanceOfJsonArray(Object json) {
		return json == null ? false : json instanceof JSONArray || json instanceof JJson;
	}

	//
	private static String[] stringRegex(String regex, String input) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(input);

		ArrayList<String> alMatch = new ArrayList<>();
		while (m.find()) {
			alMatch.add(m.group());
		}
		String[] matchs = new String[alMatch.size()];
		for (int i = 0; i < matchs.length; i++) {
			matchs[i] = alMatch.get(i);
		}
		return matchs.length == 0 ? null : matchs;
	}

	private static String strToInt(String fl) {
		if (fl.contains(".")) {
			return fl.substring(0, fl.indexOf("."));
		}
		return fl;
	}

	private static float sumFloat(float[] fls) {
		float fl = 0;
		for (int i = 0; i < fls.length / 2; i++) {
			fl += fls[i] + fls[fls.length - 1 - i];
		}
		fl += fls.length % 2 == 0 ? 0 : fls[fls.length / 2];
		return fl;
	}

	private static String arraysToString(Object[] objs) {
		StringBuilder stringBuilder = new StringBuilder("[");
		for (Object obj : objs) {
			String str = obj == null ? "" : obj.toString().replaceAll("\"", "\\\\\"");
			stringBuilder.append("\"").append(str).append("\",");
		}

		return stringBuilder.toString().substring(0, stringBuilder.length() - 1) + "]";
	}

	private static boolean isNumber(String num) {
		try {
			Float.parseFloat(num);
			return true;
		} catch (NumberFormatException | NullPointerException e) {
			return false;
		}
	}
}
