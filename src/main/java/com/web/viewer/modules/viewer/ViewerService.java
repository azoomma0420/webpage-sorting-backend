package com.web.viewer.modules.viewer;

import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ViewerService {
    public String getData(String url, boolean includeHTML) throws IOException, HttpCallException {
        String htmlData = getHttpData(url);
        if(!includeHTML) {
            //정규식으로 HTML 태그를 없애준다.
            htmlData = htmlData.replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", "");
        }
        return htmlData;
    }

    public HtmlDataDTO getConversionData(String url, Integer unit, boolean includeHTML) throws IOException, HttpCallException {
        String htmlData = getData(url, includeHTML);

        //알파벳과 숫자를 가져오고
        List<String> alphabets = getAlphabets(htmlData);
        List<String> numbers = getNumbers(htmlData);

        //오름차순 정렬하고
        alphabets = ascending(alphabets);
        numbers = ascending(numbers);
        
        //단위로 쪼개서 교차한다.
        String result = mix(alphabets, numbers, unit);

        Integer quotient =  (alphabets.size()+numbers.size()) / unit;
        Integer remainder = (alphabets.size()+numbers.size()) % unit;
        return HtmlDataDTO.builder()
                        .quotient(quotient)
                        .remainder(remainder)
                        .result(result).build();
    }

    private String mix(List<String> alphabets, List<String> numbers, Integer unit) {
        String result = "";
        int min = Math.min(alphabets.size(), numbers.size());
        int remainder = min % unit;
        for(int i=0; i<(min-remainder); i+=unit) {
            result += getStringFromList(alphabets, i, unit);
            result += getStringFromList(numbers, i, unit);
        }
        return result;
    }

    private String getStringFromList(List<String> list, int index, Integer unit) {
        String result = "";
        for(int i=index; i<(index+unit); i++) {
            result += list.get(i);
        }
        return result;
    }

    private List<String> ascending(List<String> list) {
        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
        return list;
    }

    private List<String> getAlphabets(String str) {
        ArrayList<String> list = new ArrayList<>();
        Matcher matcher = Pattern.compile("[a-zA-Z]").matcher(str);
        while (matcher.find()) {
            list.add(matcher.group());
        }
        return list;
    }

    private List<String> getNumbers(String str) {
        IntStream stream = str.chars();
        return stream.filter((ch)-> (48 <= ch && ch <= 57))
                .mapToObj(ch -> (char)ch)
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    private String getHttpData(String url) throws IOException, HttpCallException{
        String text;
        HttpGet get = new HttpGet(url);
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(get)) {
            int status = response.getStatusLine().getStatusCode();
            if(status != HttpStatus.SC_OK){
                throw new HttpCallException("status code:"+status);
            }
            text = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
        }
        catch (HttpCallException | IOException e) {
            throw e;
        }
        return text;
    }
}
