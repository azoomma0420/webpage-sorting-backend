package com.web.viewer.modules.viewer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HtmlDataDTO {
    private Integer quotient;
    private Integer remainder;
    private String result;
}
