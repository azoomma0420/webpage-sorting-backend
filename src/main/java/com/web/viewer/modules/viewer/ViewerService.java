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
import java.util.Arrays;
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
        String alphabets = getAlphabets(htmlData);
        String numbers = getNumbers(htmlData);

        //오름차순 정렬하고
        alphabets = ascending(alphabets);
        numbers = ascending(numbers);
        
        //단위로 쪼개서 교차한다.
        return mix(alphabets, numbers, unit);
    }

    private HtmlDataDTO mix(String alphabets, String numbers, Integer unit) {
        Integer min = Math.min(alphabets.length(), numbers.length());
        Integer quotient =  min / unit;
        Integer remainder = min % unit;
        String result = "";
        for(int i=0; i< min; i +=unit) {
            if(i+unit < alphabets.length())
                result += alphabets.substring(i, i+unit);

            if(i+unit < numbers.length())
                result += numbers.substring(i, i+unit);
        }
        return HtmlDataDTO.builder()
                    .quotient(quotient)
                    .remainder(remainder)
                    .result(result).build();
    }

    private String ascending(String str) {
        char[] temp = str.toCharArray();
        Arrays.sort(temp);
        return new String(temp);
    }

    private String getAlphabets(String str) {
        String result = "";
        Matcher matcher = Pattern.compile("[a-zA-Z]").matcher(str);
        while (matcher.find()) {
            result += matcher.group();
        }
        return result;
    }

    private String getNumbers(String str) {
        IntStream stream = str.chars();
        String intStr = stream.filter((ch)-> (48 <= ch && ch <= 57))
                .mapToObj(ch -> (char)ch)
                .map(Object::toString)
                .collect(Collectors.joining());
        return intStr;
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
