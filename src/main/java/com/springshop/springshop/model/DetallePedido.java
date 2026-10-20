package com.springshop.springshop.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Guarda una copia de los datos del producto que se vendió, independiente del catálogo.
 */
public class DetallePedido {

	private final Long idProducto;
	private final String nombreProducto;
	private final int cantidad;
	private final BigDecimal precioUnitario;
	private final BigDecimal subtotal;

	public DetallePedido(Long idProducto, String nombreProducto, int cantidad, BigDecimal precioUnitario) {
		this.idProducto = Objects.requireNonNull(idProducto, "El identificador del producto es obligatorio");
		this.nombreProducto = Objects.requireNonNull(nombreProducto, "El nombre del producto es obligatorio");
		if (cantidad < 1) {
			throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
		}
		this.cantidad = cantidad;
		this.precioUnitario = Objects.requireNonNull(precioUnitario, "El precio unitario es obligatorio");
		this.subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
	}

	public Long getIdProducto() {
		return idProducto;
	}

	public String getNombreProducto() {
		return nombreProducto;
	}

	public int getCantidad() {
		return cantidad;
	}

	public BigDecimal getPrecioUnitario() {
		return precioUnitario;
	}

	/**
	 * Devuelve el subtotal calculado al crear el detalle, sin consultar el catálogo.
	 */
	public BigDecimal getSubtotal() {
		return subtotal;
	}
}
