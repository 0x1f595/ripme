package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MangaReadRipper extends AbstractHTMLRipper {
    public MangaReadRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "mangaread";
    }

    @Override
    public String getDomain() {
        return "mangaread.org";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https://www.mangaread.org/manga/([a-zA-Z0-9_-]+)/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected mangaread.org URL format: " +
                "www.mangaread.org/manga/[title] - got " + url + " instead");
    }

    @Override
    public Document getFirstPage() throws IOException {
        Document indexDoc = Http.url(url).get();
        Elements hrefs = indexDoc.select(".wp-manga-chapter a");
        if (hrefs.isEmpty()) {
            throw new IOException("No link to initial chapter found");
        }
        String nextUrl = hrefs.first().attr("href");
        return Http.url(nextUrl).get();
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        Elements hrefs = doc.select("a.prev_page");
        if (hrefs.isEmpty()) {
            throw new IOException("No more pages");
        }
        String nextUrl = hrefs.first().attr("href");
        sleep(500);
        return Http.url(nextUrl).get();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        for (Element el : doc.select(".reading-content img")) {
            result.add(el.attr("src"));
        }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url);
    }
}
