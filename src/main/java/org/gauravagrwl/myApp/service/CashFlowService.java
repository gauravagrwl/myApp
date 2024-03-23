package org.gauravagrwl.myApp.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.gauravagrwl.myApp.model.reports.CashFlowReportDocument;
import org.gauravagrwl.myApp.model.repositories.CashFlowReportDocumentRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

@Service
public class CashFlowService {

    CashFlowReportDocumentRepository cashFlowReportDocumentRepository;

    public CashFlowService(CashFlowReportDocumentRepository cashFlowReportDocumentRepository) {
        this.cashFlowReportDocumentRepository = cashFlowReportDocumentRepository;
    }

    // TODO:
    // From username get accounts id.
    // Stream Cashflow report and get based on account id.

    public Set<Integer> getCashFlowReportYearList(String userName) {
        List<CashFlowReportDocument> all = cashFlowReportDocumentRepository.findAll();

        return all.stream().map(CashFlowReportDocument::getYear).collect(Collectors.toSet());
    }

    public List<CashFlowReportDocument> getCashFlowReportsByYear(String useString) {

        return null;
    }

}
