package de.unibayreuth.bayceer.oc.search.lucene;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.AbstractMap.SimpleEntry;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class DcParser {

	// https://regex101.com/
	// Matches all key values
	private static final Pattern p = Pattern.compile("^([a-zA-Z]+):(.*)");

	public static List<SimpleEntry<String, String>> parse(String content) throws DcParserException {
		List<SimpleEntry<String, String>> ret = new ArrayList<SimpleEntry<String, String>>(10);
		try (BufferedReader br = new BufferedReader(new StringReader(content))) {
			String line;
			Boolean onKey = false;
			while ((line = br.readLine()) != null) {
				Matcher matcher = p.matcher(line);
				if (matcher.matches()) {
					String key = matcher.group(1);
					String value = matcher.group(2);
					if (value.isEmpty()) {
						onKey = false;
					} else {
						ret.add(new SimpleEntry<String, String>(key, value));
						onKey = true;
					}

				} else {
					if (ret.size() > 0 && onKey) {
						SimpleEntry<String, String> lastEntry = ret.get(ret.size() - 1);
						StringBuffer b = new StringBuffer(lastEntry.getValue());
						b.append("\n");
						b.append(line);
						lastEntry.setValue(b.toString());
					}

				}

			}
		} catch (IOException e) {
			throw new DcParserException(e.getMessage());
		}

		return ret;
	}

}
