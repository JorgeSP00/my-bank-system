package com.bank.accountservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

import com.bank.accountservice.dto.request.AccountRequestDTO;
import com.bank.accountservice.dto.response.AccountResponseDTO;
import com.bank.accountservice.mapper.AccountMapper;
import com.bank.accountservice.model.account.Account;
import com.bank.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controlador REST para gestión de cuentas.
 * Expone endpoints para operaciones CRUD de cuentas.
 */
@RestController
@RequestMapping("/bank_system/accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Account Management", description = "APIs para gestionar cuentas bancarias")
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @GetMapping
    @Operation(summary = "Obtener todas las cuentas", description = "Recupera una lista de todas las cuentas bancarias")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de cuentas obtenida exitosamente",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountResponseDTO.class)))
    })
    public List<AccountResponseDTO> getAll() {
        log.info("[AccountController] GET /bank_system/accounts");
        return accountService.findAllAccounts()
                .stream()
                .map(accountMapper::fromEntityToResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cuenta por ID", description = "Recupera una cuenta específica por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cuenta encontrada",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Cuenta no encontrada", content = @Content)
    })
    public ResponseEntity<AccountResponseDTO> getAccountById(
            @PathVariable UUID id) {
        log.info("[AccountController] GET /bank_system/accounts/{}", id);
        Account account = accountService.getAccountEntityById(id);
        return ResponseEntity.ok(accountMapper.fromEntityToResponse(account));
    }

    @PostMapping
    @Operation(summary = "Crear nueva cuenta", description = "Crea una nueva cuenta bancaria")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cuenta creada exitosamente",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos de solicitud inválidos", content = @Content),
        @ApiResponse(responseCode = "409", description = "Cuenta ya existe", content = @Content)
    })
    public ResponseEntity<AccountResponseDTO> createAccount(
            @Valid @RequestBody AccountRequestDTO dto) {
        log.info("[AccountController] POST /bank_system/accounts - AccountNumber: {}, Owner: {}", 
            dto.getAccountNumber(), dto.getOwnerName());
        Account account = accountMapper.fromRequestToEntity(dto);
        Account created = accountService.createAccount(account);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountMapper.fromEntityToResponse(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cuenta", description = "Actualiza una cuenta existente por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cuenta actualizada exitosamente",
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos de solicitud inválidos", content = @Content),
        @ApiResponse(responseCode = "404", description = "Cuenta no encontrada", content = @Content)
    })
    public ResponseEntity<AccountResponseDTO> updateAccount(
            @PathVariable UUID id,
            @Valid @RequestBody AccountRequestDTO dto) {
        Account account = accountMapper.fromRequestToEntity(dto);
        account.setId(id);
        Account updated = accountService.updateAccount(account);
        return ResponseEntity.ok(accountMapper.fromEntityToResponse(updated));
    }
}