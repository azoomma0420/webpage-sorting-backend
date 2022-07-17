package com.web.viewer.modules.viewer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HtmlDataDTO {
    private Integer quotientN;
    private Integer remainderN;
    private String quotient;
    private String remainder1;
    private String remainder2;
}
