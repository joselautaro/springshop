package com.springshop.springshop.model;

import java.math.BigDecimal;

/**
 * Representa un producto del catálogo con su precio y disponibilidad.
 */
public class Producto {

	private final Long id;
	private final String nombre;
	private final String descripcion;
	private final BigDecimal precio;
	private final int stock;

	public Producto(Long id, String nombre, String descripcion, BigDecimal precio, int stock) {
		this.id = id;
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.precio = precio;
		this.stock = stock;
	}

	public Long getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public BigDecimal getPrecio() {
		return precio;
	}

	public int getStock() {
		return stock;
	}
}
