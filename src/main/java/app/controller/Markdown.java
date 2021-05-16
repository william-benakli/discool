package app.controller;

import com.vaadin.flow.component.Html;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class does all the transformations to and from Markdown with the help of https://github.com/vsch/flexmark-java
 */
public class Markdown {


    /**
     * Converts a String from Markdown to html, ready to be added to any component
     *
     * @param markdown The String to convert
     * @return The HTML object containing the correct code
     */
    public static Html getHtmlFromMarkdown(String markdown) {
        String converted = convertStringFromMarkdown(markdown.replaceAll("\n","<br>"));
        return new Html("<span>" + converted + "</span>");
    }

    /**
     * Converts a markdown string into the corresponding html text.
     *
     * @param markdown The markdown string to convert
     * @return the HTML code
     */
    private static String convertStringFromMarkdown(String markdown) {
        if (markdown.equals("")) {
            return markdown;
        }
        // init the parser and renderer
        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();
        // parse and render the String
        Node tmp = parser.parse(markdown);
        String htmlText = renderer.render(tmp);
        // remove the enclosing <p> tags
        Pattern pattern = Pattern.compile("<p>(.+?)</p>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(htmlText);
        htmlText = (matcher.find())?matcher.group(1):htmlText;
        return htmlText;
    }
}
