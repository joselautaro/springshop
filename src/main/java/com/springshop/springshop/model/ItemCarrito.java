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

	void incrementarCantidad() {
		cantidad++;
	}
}
