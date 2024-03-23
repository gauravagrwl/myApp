package org.gauravagrwl.myApp.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

import org.gauravagrwl.myApp.service.CashFlowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping(value = "/report")
public class ReportController {

    CashFlowService cashFlowService;

    public ReportController(CashFlowService cashFlowService) {
        this.cashFlowService = cashFlowService;
    }

    @GetMapping("/getCashFlowReportYeaer")
    public ResponseEntity<Set<Integer>> getCashFlowReportYeaer(@RequestParam(required = false) String userName) {

        Set<Integer> cashFlowReportYearList = cashFlowService.getCashFlowReportYearList(userName);

        return ResponseEntity.ok(cashFlowReportYearList);
    }

}
