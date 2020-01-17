package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.json.JSONArray;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

// Partially based on the implementations here:
// https://github.com/davidteather/TikTok-Api
// https://github.com/tolgatasci/musically-tiktok-api-python
// TODO: convert this to a non-HTML ripper since we're mostly working with JSON
public class TikTokRipper extends AbstractHTMLRipper {
    private Map<String, String> itemIDs = Collections.synchronizedMap(new HashMap<String, String>());

    protected String secUid;
    protected String userId;
    protected Integer page = 0;
    protected String signature = "pN0c4gAgEBDRLJe8E9p926TdHfAAPru";

    public TikTokRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "tiktok";
    }

    @Override
    public String getDomain() {
        return "tiktok.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://www." + getDomain() + "/@([a-zA-Z0-9_-]+)$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException(
                "Expected " + getDomain() + " URL format: " +
                getDomain() + "/@username - got " + url + " instead");
    }

    @Override
    public Document getFirstPage() throws IOException {
        Document sourceDoc = Http.url(url).get();

        // Get the user IDs to query the API
        Element el = sourceDoc.getElementById("__NEXT_DATA__");
        JSONObject obj = new JSONObject(el.data());
        JSONObject userData = obj.getJSONObject("props").getJSONObject("pageProps").getJSONObject("userData");
        secUid = userData.getString("secUid");
        userId = userData.getString("userId");

        // TODO: update signature

        return getApiPage();
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        Elements hrefs = doc.select(".h-entry + .entry > a.load-more.load-gap");
        if (hrefs.isEmpty()) {
            throw new IOException("No more pages");
        }
        String nextUrl = hrefs.last().attr("href");
        sleep(500);
        return Http.url(nextUrl).get();
    }

    protected Document getApiPage() throws IOException {
        String contentUrl = "https://m.tiktok.com/share/item/list?secUid=" + secUid
            + "&id=" + userId + "&type=1&count=30&minCursor=0&maxCursor=0&shareUid="
            + "&_signature=" + signature;
        Document doc = Http.url(contentUrl)
            .header("Referer", url.toString())
            .header("User-agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1")
            .get();
        return doc;
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        JSONObject obj = new JSONObject(doc.data());
        JSONArray itemList = obj.getJSONObject("body").getJSONArray("itemListData");

        // TODO: convert the list data to something usable

        List<String> result = new ArrayList<String>();
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, itemIDs.get(url.toString()) + "_");
    }
}
