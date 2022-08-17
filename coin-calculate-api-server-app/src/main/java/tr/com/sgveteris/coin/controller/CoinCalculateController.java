package tr.com.sgveteris.coin.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tr.com.sgveteris.coin.rest.CoinCalculateRequest;
import tr.com.sgveteris.coin.rest.CoinCalculateResponse;
import tr.com.sgveteris.coin.service.ICoinCalculateService;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api")
@Api(value = "coinCalculate")
@RequiredArgsConstructor
public class CoinCalculateController {

    private final ICoinCalculateService calculateService;


    @PostMapping(value = {"/coin/detail"})
    @ApiOperation(value = "Do a coin request", response = CoinCalculateResponse.class, httpMethod = "POST")
    public ResponseEntity<CoinCalculateResponse> fetchCoinDetail(@RequestBody @Valid CoinCalculateRequest request) {
        return ResponseEntity.ok(calculateService.getFetchCoinDetail(request));
    }


}
