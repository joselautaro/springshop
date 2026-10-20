package com.springshop.springshop.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Datos enviados para confirmar el checkout; los importes no forman parte del formulario.
 */
public class CheckoutForm {

	@NotBlank(message = "Ingresá tu nombre completo.")
	private String nombreCliente;

	@NotBlank(message = "Ingresá tu correo electrónico.")
	@Email(message = "Ingresá un correo electrónico válido.")
	private String emailCliente;

	@NotBlank(message = "Ingresá la dirección de entrega.")
	private String direccionEntrega;

	@NotBlank(message = "El token de checkout es obligatorio.")
	private String tokenCheckout;

	public String getNombreCliente() {
		return nombreCliente;
	}

	public void setNombreCliente(String nombreCliente) {
		this.nombreCliente = nombreCliente;
	}

	public String getEmailCliente() {
		return emailCliente;
	}

	public void setEmailCliente(String emailCliente) {
		this.emailCliente = emailCliente;
	}

	public String getDireccionEntrega() {
		return direccionEntrega;
	}

	public void setDireccionEntrega(String direccionEntrega) {
		this.direccionEntrega = direccionEntrega;
	}

	public String getTokenCheckout() {
		return tokenCheckout;
	}

	public void setTokenCheckout(String tokenCheckout) {
		this.tokenCheckout = tokenCheckout;
	}
}
