package com.speed.toncore.accounts.controller;

import com.speed.toncore.util.SecurityManagerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@RestController
@Slf4j
public class TonSecretManagerController {

	@GetMapping("/create-secret-key/{length}")
	public ResponseEntity<String> createSecretKey(@PathVariable Integer length) throws NoSuchAlgorithmException {
		return ResponseEntity.ok(Base64.getEncoder().encodeToString(SecurityManagerUtil.generateKey(length)));
	}

	@GetMapping("/decrypt")
	public ResponseEntity<String> decrypt(@RequestBody String text) {
		return ResponseEntity.ok(SecurityManagerUtil.decrypt("AES", text, Base64.getDecoder().decode("z9x0/TNvFVgDg5VaVyuPzQnW/DJQzYUalpM6cgoeoV4=")));
	}
}
