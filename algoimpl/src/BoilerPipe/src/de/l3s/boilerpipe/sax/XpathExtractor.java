/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.l3s.boilerpipe.sax;

/**
 *
 * @author sanojaa
 */
/**
 * boilerpipe
 *
 * Copyright (c) 2009 Christian Kohlschütter
 *
 * The author licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.IOException;
import java.net.URL;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.xerces.parsers.AbstractSAXParser;
import org.cyberneko.html.HTMLConfiguration;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import de.l3s.boilerpipe.BoilerpipeExtractor;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.document.TextBlock;
import de.l3s.boilerpipe.document.TextDocument;
import java.io.BufferedReader;
import java.io.Reader;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;

/**
 * Highlights text blocks in an HTML document that have been marked as "content"
 * in the corresponding {@link TextDocument}.
 * 
 * @author Christian Kohlschütter
 */
public final class XpathExtractor {

	/**
	 * Creates a new {@link HTMLHighlighter}, which is set-up to return the full
	 * HTML text, with the extracted text portion <b>highlighted</b>.
	 */
	public static XpathExtractor newXpathExtractorInstance() {
		return new XpathExtractor(false);
	}

	/**
	 * Creates a new {@link HTMLHighlighter}, which is set-up to return only the
	 * extracted HTML text, including enclosed markup.
	 */
	public static XpathExtractor newExtractingInstance() {
		return new XpathExtractor(true);
	}

	private XpathExtractor(final boolean extractHTML) {
		if (extractHTML) {
			setOutputHighlightOnly(true);
			setExtraStyleSheet("\n<style type=\"text/css\">\n"
			+ "A:before { content:' '; } \n"  //
			+ "A:after { content:' '; } \n"  //
			+ "SPAN:before { content:' '; } \n"  //
			+ "SPAN:after { content:' '; } \n"  //
			+ "</style>\n");
			setPreHighlight("");
			setPostHighlight("");
		}
	}

		
	private static final Pattern PAT_TAG_NO_TEXT = Pattern.compile("<[^/][^>]*></[^>]*>");
	private static final Pattern PAT_SUPER_TAG = Pattern.compile("^<[^>]*>(<.*?>)</[^>]*>$");
public static int countWords(String s){

    int wordCount = 0;

    boolean word = false;
    int endOfLine = s.length() - 1;

    for (int i = 0; i < s.length(); i++) {
        // if the char is a letter, word = true.
        if (Character.isLetter(s.charAt(i)) && i != endOfLine) {
            word = true;
            // if char isn't a letter and there have been letters before,
            // counter goes up.
        } else if (!Character.isLetter(s.charAt(i)) && word) {
            wordCount++;
            word = false;
            // last word of String; if it doesn't end with a non letter, it
            // wouldn't count without this.
        } else if (Character.isLetter(s.charAt(i)) && i == endOfLine) {
            wordCount++;
        }
    }
    return wordCount;
}
	public String process(final URL url, final BoilerpipeExtractor extractor)
			throws IOException, BoilerpipeProcessingException, SAXException {
		final HTMLDocument htmlDoc = HTMLFetcher.fetch(url);
                int tdcount = countWords(Jsoup.parse(new String(htmlDoc.getData())).text());
		final TextDocument doc = new BoilerpipeSAXInput(htmlDoc.toInputSource()).getTextDocument();
                int sid = 1;
                String output = "var blocks = [";
		extractor.process(doc);

		
                for (TextBlock block : doc.getTextBlocks()) {
                    output += "[\"B"+(new Integer(sid).toString())+"\",";
                    output += StringUtils.join(block.getGeometryList().toArray(),",");
                    output += "],";
                    sid++;
		}
                output += "undefined];";
		return(output);
	}

	private boolean outputHighlightOnly = false;
	private String extraStyleSheet = "\n<style type=\"text/css\">\n"
			+ ".x-boilerpipe-mark1 {" + " text-decoration:none; "
			+ "background-color: #ffff42 !important; "
			+ "color: black !important; " + "display:inline !important; "
			+ "visibility:visible !important; }\n" + //
			"</style>\n";
	private String preHighlight = "<span class=\"x-boilerpipe-mark1\">";
	private String postHighlight = "</span>";

	/**
	 * If true, only HTML enclosed within highlighted content will be returned
	 */
	public boolean isOutputHighlightOnly() {
		return outputHighlightOnly;
	}

	/**
	 * Sets whether only HTML enclosed within highlighted content will be
	 * returned, or the whole HTML document.
	 */
	public void setOutputHighlightOnly(boolean outputHighlightOnly) {
		this.outputHighlightOnly = outputHighlightOnly;
	}

	/**
	 * Returns the extra stylesheet definition that will be inserted in the HEAD
	 * element.
	 * 
	 * By default, this corresponds to a simple definition that marks text in
	 * class "x-boilerpipe-mark1" as inline text with yellow background.
	 */
	public String getExtraStyleSheet() {
		return extraStyleSheet;
	}

	/**
	 * Sets the extra stylesheet definition that will be inserted in the HEAD
	 * element.
	 * 
	 * To disable, set it to the empty string: ""
	 * 
	 * @param extraStyleSheet
	 *            Plain HTML
	 */
	public void setExtraStyleSheet(String extraStyleSheet) {
		this.extraStyleSheet = extraStyleSheet;
	}

	/**
	 * Returns the string that will be inserted before any highlighted HTML
	 * block.
	 * 
	 * By default, this corresponds to
	 * <code>&lt;span class=&qupt;x-boilerpipe-mark1&quot;&gt;</code>
	 */
	public String getPreHighlight() {
		return preHighlight;
	}

	/**
	 * Sets the string that will be inserted prior to any highlighted HTML
	 * block.
	 * 
	 * To disable, set it to the empty string: ""
	 */
	public void setPreHighlight(String preHighlight) {
		this.preHighlight = preHighlight;
	}

	/**
	 * Returns the string that will be inserted after any highlighted HTML
	 * block.
	 * 
	 * By default, this corresponds to <code>&lt;/span&gt;</code>
	 */
	public String getPostHighlight() {
		return postHighlight;
	}

	/**
	 * Sets the string that will be inserted after any highlighted HTML block.
	 * 
	 * To disable, set it to the empty string: ""
	 */
	public void setPostHighlight(String postHighlight) {
		this.postHighlight = postHighlight;
	}

	private abstract static class TagAction {
		void beforeStart(final Implementation instance, final String localName) {
		}

		void afterStart(final Implementation instance, final String localName) {
		}

		void beforeEnd(final Implementation instance, final String localName) {
		}

		void afterEnd(final Implementation instance, final String localName) {
		}
	}

	private static final TagAction TA_IGNORABLE_ELEMENT = new TagAction() {
		void beforeStart(final Implementation instance, final String localName) {
			instance.inIgnorableElement++;
		}

		void afterEnd(final Implementation instance, final String localName) {
			instance.inIgnorableElement--;
		}
	};

	private static final TagAction TA_HEAD = new TagAction() {
		void beforeStart(final Implementation instance, final String localName) {
			instance.inIgnorableElement++;
		}

		void beforeEnd(final Implementation instance, String localName) {
			instance.html.append(instance.hl.extraStyleSheet);
		}

		void afterEnd(final Implementation instance, final String localName) {
			instance.inIgnorableElement--;
		}
	};
	private static Map<String, TagAction> TAG_ACTIONS = new HashMap<String, TagAction>();
	static {
		TAG_ACTIONS.put("STYLE", TA_IGNORABLE_ELEMENT);
		TAG_ACTIONS.put("SCRIPT", TA_IGNORABLE_ELEMENT);
		TAG_ACTIONS.put("OPTION", TA_IGNORABLE_ELEMENT);
		TAG_ACTIONS.put("NOSCRIPT", TA_IGNORABLE_ELEMENT);
		TAG_ACTIONS.put("OBJECT", TA_IGNORABLE_ELEMENT);
		TAG_ACTIONS.put("EMBED", TA_IGNORABLE_ELEMENT);
		TAG_ACTIONS.put("APPLET", TA_IGNORABLE_ELEMENT);
		// NOTE: you might want to comment this out:
		TAG_ACTIONS.put("LINK", TA_IGNORABLE_ELEMENT);

		TAG_ACTIONS.put("HEAD", TA_HEAD);
	}

	private final class Implementation extends AbstractSAXParser implements
			ContentHandler {
		StringBuilder html = new StringBuilder();

		private int inIgnorableElement = 0;
		private int characterElementIdx = 0;
		private final BitSet contentBitSet = new BitSet();
		private final XpathExtractor hl = XpathExtractor.this;

		Implementation() {
			super(new HTMLConfiguration());
			setContentHandler(this);
		}

		void process(final TextDocument doc, final InputSource is)
				throws BoilerpipeProcessingException {
			for (TextBlock block : doc.getTextBlocks()) {
//				if (block.isContent()) {
//					final BitSet bs = block.getContainedTextElements();
//					if (bs != null) {
//						contentBitSet.or(bs);
//					}
//				}
                                System.out.println(block.getGeometry().toString());
			}

//			try {
//				parse(is);
//			} catch (SAXException e) {
//				throw new BoilerpipeProcessingException(e);
//			} catch (IOException e) {
//				throw new BoilerpipeProcessingException(e);
//			}
		}

		public void endDocument() throws SAXException {
		}

		public void endPrefixMapping(String prefix) throws SAXException {
		}

		public void ignorableWhitespace(char[] ch, int start, int length)
				throws SAXException {
		}

		public void processingInstruction(String target, String data)
				throws SAXException {
		}

		public void setDocumentLocator(Locator locator) {
		}

		public void skippedEntity(String name) throws SAXException {
		}

		public void startDocument() throws SAXException {
		}

		public void startElement(String uri, String localName, String qName,
				Attributes atts) throws SAXException {
			TagAction ta = TAG_ACTIONS.get(localName);
			if (ta != null) {
				ta.beforeStart(this, localName);
			}

			// HACK: remove existing highlight
			boolean ignoreAttrs = false;
			if ("SPAN".equalsIgnoreCase(localName)) {
				String classVal = atts.getValue("class");
				if ("x-boilerpipe-mark1".equals(classVal)) {
					ignoreAttrs = true;
				}
			}

			try {
				if (inIgnorableElement == 0) {
					if (outputHighlightOnly) {
//						boolean highlight = contentBitSet
//								.get(characterElementIdx);

//						if (!highlight) {
//							return;
//						}
					}

					html.append('<');
					html.append(qName);
					if (!ignoreAttrs) {
						final int numAtts = atts.getLength();
						for (int i = 0; i < numAtts; i++) {
							final String attr = atts.getQName(i);
							final String value = atts.getValue(i);
							html.append(' ');
							html.append(attr);
							html.append("=\"");
							html.append(xmlEncode(value));
							html.append("\"");
						}
					}
					html.append('>');
				}
			} finally {
				if (ta != null) {
					ta.afterStart(this, localName);
				}
			}
		}

		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			TagAction ta = TAG_ACTIONS.get(localName);
			if (ta != null) {
				ta.beforeEnd(this, localName);
			}

			try {
				if (inIgnorableElement == 0) {
					if (outputHighlightOnly) {
//						boolean highlight = contentBitSet
//								.get(characterElementIdx);

//						if (!highlight) {
//							return;
//						}
					}
					html.append("</");
					html.append(qName);
					html.append('>');
				}
			} finally {
				if (ta != null) {
					ta.afterEnd(this, localName);
				}
			}
		}

		public void characters(char[] ch, int start, int length)
				throws SAXException {
			characterElementIdx++;
			if (inIgnorableElement == 0) {

				boolean highlight = contentBitSet.get(characterElementIdx);

				if (!highlight && outputHighlightOnly) {
					return;
				}

				if (highlight) {
					html.append(preHighlight);
				}
				html.append(xmlEncode(String.valueOf(ch, start, length)));
				if (highlight) {
					html.append(postHighlight);
				}
			}
		}

		public void startPrefixMapping(String prefix, String uri)
				throws SAXException {
		}

	}

	private static String xmlEncode(final String in) {
		if (in == null) {
			return "";
		}
		char c;
		StringBuilder out = new StringBuilder(in.length());

		for (int i = 0; i < in.length(); i++) {
			c = in.charAt(i);
			switch (c) {
			case '<':
				out.append("&lt;");
				break;
			case '>':
				out.append("&gt;");
				break;
			case '&':
				out.append("&amp;");
				break;
			case '"':
				out.append("&quot;");
				break;
			default:
				out.append(c);
			}
		}

		return out.toString();
	}

}
