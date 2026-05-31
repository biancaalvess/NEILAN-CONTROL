package com.neilan.control.controller.api;

import com.neilan.control.service.FinanceiroService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/export")
public class ExportApiController {

    private final FinanceiroService financeiroService;

    public ExportApiController(FinanceiroService financeiroService) {
        this.financeiroService = financeiroService;
    }

    @GetMapping("/csv")
    public ResponseEntity<byte[]> exportarCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

        LocalDate hoje = LocalDate.now();
        if (inicio == null) {
            inicio = hoje.withDayOfMonth(1);
        }
        if (fim == null) {
            fim = hoje;
        }

        String csv = financeiroService.gerarCsv(inicio, fim);
        String filename = "neilan-servicos-" + inicio + "-" + fim + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8));
    }
}
