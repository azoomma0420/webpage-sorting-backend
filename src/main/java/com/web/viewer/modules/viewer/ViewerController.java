package com.web.viewer.modules.viewer;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ViewerController {
    private final ViewerService viewerService;

    @GetMapping(value = {"/data"})
    public ResponseEntity<?> getData(@RequestParam("url") String url) {
        try {
            return new ResponseEntity<>(viewerService.getData(url), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
