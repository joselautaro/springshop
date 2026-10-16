package com.springshop.springshop.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Representa una línea del carrito: un producto y la cantidad elegida.
 */
public class ItemCarrito {

	private final Producto producto;
	private int cantidad;

	public ItemCarrito(Producto producto, int cantidad) {
		this.producto = Objects.requireNonNull(producto, "El producto es obligatorio");
		if (cantidad < 1) {
			throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
		}
		this.cantidad = cantidad;
	}

	public Producto getProducto() {
		return producto;
	}

	public int getCantidad() {
		return cantidad;
	}

	/**
	 * Calcula el subtotal multiplicando el precio por la cantidad, sin usar punto flotante.
	 */
	public BigDecimal getSubtotal() {
		return producto.getPrecio().multiply(BigDecimal.valueOf(cantidad));
	}

	/**
	 * Incrementa la línea solo si queda stock vigente disponible para otra unidad.
	 */
	void incrementarCantidad(int stockVigente) {
		if (stockVigente < 0 || cantidad >= stockVigente) {
			throw new IllegalArgumentException("No hay stock suficiente para aumentar la cantidad");
		}
		cantidad++;
	}

	/**
	 * Reduce una unidad sin permitir que la cantidad de la línea llegue a cero.
	 */
	void disminuirCantidad() {
		if (cantidad <= 1) {
			throw new IllegalArgumentException("La cantidad no puede ser menor que uno");
		}
		cantidad--;
	}
}
