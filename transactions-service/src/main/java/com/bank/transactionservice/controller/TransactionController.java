package com.bank.transactionservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;

import com.bank.transactionservice.dto.request.TransactionRequestDTO;
import com.bank.transactionservice.dto.response.TransactionResponseDTO;
import com.bank.transactionservice.mapper.TransactionMapper;
import com.bank.transactionservice.model.transaction.Transaction;
import com.bank.transactionservice.service.TransactionService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controlador REST para gestión de transacciones.
 * Expone endpoints para operaciones CRUD de transacciones.
 */
@RestController
@RequestMapping("/bank_system/transactionservice/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transaction Management", description = "APIs para gestionar transacciones bancarias")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;
    
    @GetMapping
    @Operation(summary = "Obtener todas las transacciones", description = "Recupera una lista de todas las transacciones")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de transacciones obtenida exitosamente",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionResponseDTO.class)))
    })
    public ResponseEntity<List<TransactionResponseDTO>> getAllTransactions() {
        log.info("[TransactionController] GET /bank_system/transactionservice/transactions");
        return ResponseEntity.ok(transactionService.getAllTransactions()
                .stream()
                .map(transactionMapper::fromEntityToResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener transacción por ID", description = "Recupera una transacción específica por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transacción encontrada",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Transacción no encontrada", content = @Content)
    })
    public ResponseEntity<TransactionResponseDTO> getTransactionById(@PathVariable UUID id) {
        log.info("[TransactionController] GET /bank_system/transactionservice/transactions/{}", id);
        Transaction transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(transactionMapper.fromEntityToResponse(transaction));
    }

    @PostMapping
    @Operation(summary = "Crear nueva transacción", description = "Crea una nueva transacción bancaria")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Transacción creada exitosamente",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos de solicitud inválidos", content = @Content),
        @ApiResponse(responseCode = "422", description = "Datos de transacción inválidos", content = @Content)
    })
    public ResponseEntity<TransactionResponseDTO> createTransaction(@Valid @RequestBody TransactionRequestDTO transactionRequestDTO) {
        log.info("[TransactionController] POST /bank_system/transactionservice/transactions - FromAccount: {}, ToAccount: {}, Amount: {}", 
            transactionRequestDTO.getFromAccountNumber(), transactionRequestDTO.getToAccountNumber(), transactionRequestDTO.getAmount());
        Transaction t = transactionService.createTransaction(
            transactionRequestDTO.getFromAccountNumber(),
            transactionRequestDTO.getToAccountNumber(),
            transactionRequestDTO.getAmount(),
            transactionRequestDTO.getType(),
            transactionRequestDTO.getDescription()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionMapper.fromEntityToResponse(t));
    }
}