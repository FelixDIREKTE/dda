package com.dd.dda.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

@Slf4j
@Service
public class HtmlConverterService {

    public String stringToHtml(String text) {

        String result = "";
        String sep="";
        int charcnt = 0;
        //List<String> links = new ArrayList<>();
        boolean firstLink = true;
        for(String s : text.split("[ ,\n]")){

            String s2 = s;
            while (s2.startsWith(".")||s2.startsWith(",")||s2.startsWith(";")||s2.startsWith(")")||s2.startsWith("(")||s2.startsWith(":")||s2.startsWith("\n")) {
                s2 = s2.substring(1);
            }
            while (s2.endsWith(".")||s2.endsWith(",")||s2.endsWith(";")||s2.endsWith(")")||s2.endsWith("(")||s2.endsWith(":")||s2.endsWith("\n")) {
                s2 = s2.substring(0, s2.length() - 1);
            }

            boolean x = isUrlWithPrefixValid(s2);
            String s3 = s;
            if(x){
                s3 = s.replace(s2, linkUrl(s2, firstLink));
                firstLink = false;
            }
            sep = charcnt == 0 ? "" : String.valueOf(text.charAt(charcnt-1));
            result = result + sep + s3;
            charcnt+=s.length()+1;

        }
        result = result.replace("\n", "<br>");
        return result;
    }

    private String linkUrl(String s2, boolean firstlink){
        String linkurl;
        if(s2.startsWith("http")) {
            linkurl = s2;
        } else {
            if(s2.startsWith("www.")){
                linkurl = "https://" + s2;
            }else {
                linkurl = "https://www." + s2;
            }
        }
        //if(firstlink) {
        //    return embeddedLink(linkurl);
        //}
        return standardLink(linkurl, s2 );
    }

    private String standardLink(String linkurl, String msg){
        return "<a href='" + linkurl + "' target=\"_blank\" rel=\"noopener noreferrer\">" + msg + "</a>";
    }

    private String embeddedLink(String linkurl){
        if(linkurl.startsWith("https://www.youtube.com/watch?v=")){
            return "<iframe src=\"" + linkurl.replace("/watch?v=","/embed/")  + "\"></iframe>";
        }
        if(linkurl.contains("facebook.com")){
            return standardLink(linkurl, linkurl);
        }
        if(linkurl.contains("twitter.com")){
            return standardLink(linkurl, linkurl);
        }
        if(linkurl.contains("youtube.com")){
            return standardLink(linkurl, linkurl);
        }
        if(linkurl.contains("google.com")){
            return standardLink(linkurl, linkurl);
        }
        return "<iframe src=\"" + linkurl  + "\"></iframe>";
    }

    public boolean isUrlWithPrefixValid(String url){
        int l = url.lastIndexOf('.');
        if(l < 3 || url.length() - l <= 2){
            return false;
        }
        return url.contains(".") && ( isUrlValid(url) || isUrlValid( "https://"+url) || isUrlValid("https://www." +url));
    }

    private boolean isUrlValid(String url) {
        try {
            URL obj = new URL(url);
            obj.toURI();
            return true;
        } catch (MalformedURLException e) {
            return false;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    // in der URL  .:/?=+&-
    // nach der URL .)(,;

    public String htmlToString(String s){
        int depth = 0;
        String s2 = s.replace("<br>", " ");
        String result = "";
        for(int i = 0; i < s2.length(); i++){
            if(s2.charAt(i) == '<'){
                depth++;
            } else {
                if (s2.charAt(i) == '>') {
                    depth--;
                } else {
                    if(depth == 0){
                        result = result + s2.charAt(i);
                    }
                }
            }

        }
        return result;
    }
}
