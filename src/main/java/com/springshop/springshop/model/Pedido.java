package com.springshop.springshop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Representa un pedido confirmado e inmutable para evitar cambios accidentales posteriores.
 */
public class Pedido {

	private final Long id;
	private final String nombreCliente;
	private final String emailCliente;
	private final String direccionEntrega;
	private final LocalDateTime fecha;
	private final List<DetallePedido> detalles;
	private final BigDecimal total;
	private final EstadoPedido estado;

	public Pedido(Long id, String nombreCliente, String emailCliente, String direccionEntrega, LocalDateTime fecha,
			List<DetallePedido> detalles, EstadoPedido estado) {
		this.id = Objects.requireNonNull(id, "El identificador del pedido es obligatorio");
		this.nombreCliente = Objects.requireNonNull(nombreCliente, "El nombre del cliente es obligatorio");
		this.emailCliente = Objects.requireNonNull(emailCliente, "El email del cliente es obligatorio");
		this.direccionEntrega = Objects.requireNonNull(direccionEntrega, "La dirección de entrega es obligatoria");
		this.fecha = Objects.requireNonNull(fecha, "La fecha del pedido es obligatoria");
		this.detalles = List.copyOf(Objects.requireNonNull(detalles, "Los detalles del pedido son obligatorios"));
		if (this.detalles.isEmpty()) {
			throw new IllegalArgumentException("El pedido debe tener al menos un detalle");
		}
		this.total = this.detalles.stream()
				.map(DetallePedido::getSubtotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		this.estado = Objects.requireNonNull(estado, "El estado del pedido es obligatorio");
	}

	public Long getId() {
		return id;
	}

	public String getNombreCliente() {
		return nombreCliente;
	}

	public String getEmailCliente() {
		return emailCliente;
	}

	public String getDireccionEntrega() {
		return direccionEntrega;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	/**
	 * Expone una lista inmodificable y detalles que también son inmutables.
	 */
	public List<DetallePedido> getDetalles() {
		return detalles;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public EstadoPedido getEstado() {
		return estado;
	}
}
